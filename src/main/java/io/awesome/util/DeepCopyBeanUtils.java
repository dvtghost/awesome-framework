package io.awesome.util;

import io.awesome.annotation.DeepCloneIgnore;
import io.awesome.enums.IAttributeEnum;
import io.awesome.mapper.TypedSet;
import io.awesome.mapper.converter.*;
import io.awesome.ui.components.SelectDto;
import io.awesome.util.springbeans.CachedIntrospectionResults;
import io.awesome.util.springbeans.ClassUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.InputStreamSource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

public class DeepCopyBeanUtils {
  private static final Logger logger = LoggerFactory.getLogger(DeepCopyBeanUtils.class);

  public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws BeansException {
    return CachedIntrospectionResults.forClass(clazz).getPropertyDescriptors();
  }

  private static final Map<Pair, BaseConverter> customConverters = new HashMap<>();
  private static final Map<Class<?>, Boolean> hasCustomConverters = new HashMap<>();

  static {
    hasCustomConverters.put(IAttributeEnum.class, true);
    hasCustomConverters.put(SelectDto.class, true);
    hasCustomConverters.put(TypedSet.class, true);
    ClassesUtil.findAllEnums("io.awesome").stream()
        .filter(IAttributeEnum.class::isAssignableFrom)
        .forEach(
            clazz -> {
              customConverters.put(
                  Pair.of(clazz, SelectDto.class),
                  new EnumToSelectDTOConverter(clazz, SelectDto.class, logger));
              customConverters.put(
                  Pair.of(SelectDto.class, clazz),
                  new SelectDTOToEnumConverter(SelectDto.class, clazz, logger));
            });
    customConverters.put(
        Pair.of(SelectDto.class, TypedSet.class),
        new SelectDTOToEnumSetConverter(SelectDto.class, TypedSet.class, logger));
    customConverters.put(
        Pair.of(TypedSet.class, SelectDto.class),
        new EnumSetToSelectDTOConverter(TypedSet.class, SelectDto.class, logger));
    customConverters.put(Pair.of(SelectDto.class, SelectDto.class),
            new SelectDTOToSelectDTOConverter(SelectDto.class, SelectDto.class, logger));

  }

  @Nullable
  public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName)
      throws BeansException {
    return CachedIntrospectionResults.forClass(clazz).getPropertyDescriptor(propertyName);
  }

  public static void copyProperties(
      Object source, Object target, @Nullable String... ignoreProperties) throws BeansException {

    Assert.notNull(source, "Source must not be null");
    Assert.notNull(target, "Target must not be null");

    PropertyDescriptor[] targetPds = getPropertyDescriptors(target.getClass());
    List<String> ignoreList =
        (ignoreProperties != null
            ? new ArrayList<>(Arrays.asList(ignoreProperties))
            : new ArrayList<>());
    ignoreList.addAll(
        ClassUtil.getAllFieldsOfAnnotation(target.getClass(), DeepCloneIgnore.class).stream()
            .map(Field::getName)
            .collect(Collectors.toList()));
    for (PropertyDescriptor targetPd : targetPds) {
      Method writeMethod = targetPd.getWriteMethod();
      if (writeMethod != null && !ignoreList.contains(targetPd.getName())) {
        PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
        if (sourcePd != null) {
          Method readMethod = sourcePd.getReadMethod();
          if (readMethod != null) {
            ResolvableType sourceResolvableType = ResolvableType.forMethodReturnType(readMethod);
            ResolvableType targetResolvableType = ResolvableType.forMethodParameter(writeMethod, 0);
            if (targetResolvableType.isAssignableFrom(sourceResolvableType)
                    && !customConverters.containsKey(
                    Pair.of(readMethod.getReturnType(), writeMethod.getParameterTypes()[0]))) {
              try {
                if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                  readMethod.setAccessible(true);
                }
                Object value = readMethod.invoke(source);
                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                  writeMethod.setAccessible(true);
                }
                writeMethod.invoke(target, value);

              } catch (Throwable ex) {
                throw new FatalBeanException(
                    "Could not copy property '" + targetPd.getName() + "' from source to target",
                    ex);
              }
            } else if (customConverters.containsKey(
                Pair.of(readMethod.getReturnType(), writeMethod.getParameterTypes()[0]))) {
              try {
                if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                  readMethod.setAccessible(true);
                }
                Object value = readMethod.invoke(source);
                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                  writeMethod.setAccessible(true);
                }
                BaseConverter converter =
                    customConverters.get(
                        Pair.of(readMethod.getReturnType(), writeMethod.getParameterTypes()[0]));
                Object targetValue =
                    converter.convert(value, targetPd.getReadMethod().invoke(target));
                writeMethod.invoke(target, targetValue);
              } catch (Throwable ex) {
                throw new FatalBeanException(
                    "Could not copy property '" + targetPd.getName() + "' from source to target",
                    ex);
              }
            }
          }
        }
      }
    }

    Arrays.stream(targetPds)
        .filter(pd -> !ignoreList.contains(pd.getName()))
        .filter(
            pd -> pd.getWriteMethod() != null && pd.getWriteMethod().getParameterTypes().length > 0)
        .filter(pd -> !isSimpleProperty(pd.getWriteMethod().getParameterTypes()[0]))
        .filter(pd -> !isConverterDefined(pd.getWriteMethod().getParameterTypes()[0]))
        .forEach(
            pd -> {
              PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), pd.getName());
              Method writeMethod = pd.getWriteMethod();
              if (sourcePd != null) {
                Method readMethod = sourcePd.getReadMethod();
                if (readMethod != null && !shouldSkip(readMethod.getReturnType())) {
                  try {
                    Object value = readMethod.invoke(source);
                    if (value == null) {
                      try {
                        value = readMethod.getReturnType().getDeclaredConstructor().newInstance();
                      } catch (Throwable tr) {
                        logger.error(
                            String.format(
                                "Error initialize for source value %s sourceType-%s",
                                pd.getName(), readMethod.getReturnType()),
                            tr);
                        throw tr;
                      }
                    }
                    Object targetValue = pd.getReadMethod().invoke(target);
                    if (targetValue == null) {
                      try {
                        targetValue =
                            writeMethod
                                .getParameterTypes()[0]
                                .getDeclaredConstructor()
                                .newInstance();
                      } catch (Throwable tr) {
                        logger.error(
                            String.format(
                                "Error initialize for target value %s targetType-%s",
                                pd.getName(), writeMethod.getParameterTypes()[0]),
                            tr);
                        throw tr;
                      }
                    }
                    String[] strArr = new String[ignoreList.size()];

                    DeepCopyBeanUtils.copyProperties(
                        value, targetValue, ignoreList.toArray(strArr));
                    writeMethod.invoke(target, targetValue);
                  } catch (Throwable e) {
                    logger.error(
                        String.format(
                            "Error doing deep copies properties %s sourceType-%s targetType-%s",
                            pd.getName(),
                            readMethod.getReturnType(),
                            writeMethod.getParameterTypes()[0]),
                        e);
                  }
                }
              }
            });
  }

  private static boolean shouldSkip(Class<?> type) {
    return isConverterDefined(type) || Collection.class.isAssignableFrom(type);
  }

  public static boolean isSimpleProperty(Class<?> type) {
    Assert.notNull(type, "'type' must not be null");
    return isSimpleValueType(type)
        || (type.isArray() && isSimpleValueType(type.getComponentType()));
  }

  public static boolean isSimpleValueType(Class<?> type) {
    return (Void.class != type
            && void.class != type
            && (ClassUtils.isPrimitiveOrWrapper(type)
                || Enum.class.isAssignableFrom(type)
                || CharSequence.class.isAssignableFrom(type)
                || Number.class.isAssignableFrom(type)
                || Date.class.isAssignableFrom(type)
                || Temporal.class.isAssignableFrom(type)
                || URI.class == type
                || URL.class == type
                || Locale.class == type
                || Class.class == type)
        || InputStreamSource.class.isAssignableFrom(type));
  }

  public static boolean isConverterDefined(Class<?> type) {
    return hasCustomConverters.containsKey(type);
  }
}
