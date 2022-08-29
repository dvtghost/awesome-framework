package io.awesome.helper;

import io.awesome.service.TracingService;
import io.awesome.ui.annotations.Trace;
import io.awesome.ui.annotations.TraceTag;
import io.awesome.ui.components.SelectDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import software.amazon.ion.Decimal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class TracingHelper {
  private static final String CHANGE_TO = " → ";
  private static final Set<Class<?>> SUPPORTED_DATA_TYPE =
      Stream.of(String.class, Integer.class, Long.class, Double.class, Boolean.class, Decimal.class)
          .collect(Collectors.toCollection(HashSet::new));

  public static <E> E clone(E entity) {
    E before = null;
    try {
      if (entity instanceof Collections) {
        // TODO: Prepare for tag with multiple entity
      } else {
        before = (E) BeanUtils.cloneBean(entity);
      }
    } catch (IllegalAccessException
        | InstantiationException
        | InvocationTargetException
        | NoSuchMethodException e) {
      log.warn("Failed to snapshot the entity " + entity.getClass().getName(), e);
    }
    return before;
  }

  public static <E> void tracing(TracingService tracingService, String action, E before, E after) {
    Map<String, String> changedValues;
    if (before != null) {
      changedValues = getDifference(before, after);
    } else {
      changedValues = new HashMap<>();
    }

    Class<?> entityClazz = before.getClass();
    writeTracing(tracingService, action, after, changedValues, entityClazz);
  }

  public static <E> void tracing(TracingService tracingService, String action, E entity) {
    Map<String, String> changedValues = new HashMap<>();
    Class<?> entityClazz = entity.getClass();
    writeTracing(tracingService, action, entity, changedValues, entityClazz);
  }

  private static <E> void writeTracing(TracingService tracingService, String action, E writingEntity,
                                       Map<String, String> changedValues, Class<?> entityClazz) {
    for (Annotation annotation : entityClazz.getDeclaredAnnotations()) {
      if (annotation.annotationType() == Trace.class) {
        Trace trace = (Trace) annotation;
        String operatorName = trace.name() + "_" + action;
        changedValues.put("tracing_operator", operatorName);
        for (Field field : entityClazz.getDeclaredFields()) {
          if (field.isAnnotationPresent(TraceTag.class)) {
            TraceTag traceTag = field.getAnnotation(TraceTag.class);
            if (traceTag.type() == TraceTag.Type.ID) {
              changedValues.computeIfAbsent(trace.name() + "_id", k -> getFieldValue(field, writingEntity));
            } else if (traceTag.ignore()) {
              changedValues.remove(field.getName());
            }
          }
        }

        tracingService.trace(operatorName, changedValues);
        break;
      }
    }
  }

  private static Map<String, String> getDifference(Object before, Object after) {
    Map<String, String> values = new HashMap<>();
    for (Field field : before.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      Object valueBefore;
      Object valueAfter;
      try {
        valueBefore = field.get(before);
        valueAfter = field.get(after);
        if (valueBefore != null && valueAfter == null) {
          values.put(field.getName(), valueBefore + CHANGE_TO + "NULL");
        } else if (valueBefore == null && valueAfter != null) {
          values.put(field.getName(), "NUll" + CHANGE_TO + valueAfter);
        } else if (valueBefore != null && !Objects.equals(valueBefore, valueAfter)) {
          if (SUPPORTED_DATA_TYPE.contains(valueBefore.getClass())) {
            values.put(field.getName(), valueBefore + CHANGE_TO + valueAfter);
          } else {
            String diff = getDifferenceForSpecificDataType(valueBefore, valueAfter);
            if (StringUtils.isNotBlank(diff)) {
              values.put(field.getName(), diff);
            }
          }
        }
      } catch (IllegalAccessException e) {
        System.out.println(e);
      }
    }
    return values;
  }

  private static String getDifferenceForSpecificDataType(Object before, Object after) {
    String value = null;
    if (before instanceof SelectDto) {
      Set<String> beforeSelectedItems =
          ((SelectDto) before)
              .getSelected().stream()
                  .map(SelectDto.SelectItem::getValue)
                  .map(String::valueOf)
                  .collect(Collectors.toSet());
      Set<String> afterSelectedItems =
          ((SelectDto) after)
              .getSelected().stream()
                  .map(SelectDto.SelectItem::getValue)
                  .map(String::valueOf)
                  .collect(Collectors.toSet());
      if (CollectionUtils.isNotEmpty(beforeSelectedItems)
          && CollectionUtils.isEmpty(afterSelectedItems)) {
        value = Arrays.toString(beforeSelectedItems.toArray()) + CHANGE_TO + "NULL";
      } else if (CollectionUtils.isEmpty(beforeSelectedItems)
          && CollectionUtils.isNotEmpty(afterSelectedItems)) {
        value = "NULL::" + Arrays.toString(afterSelectedItems.toArray());
      } else if (CollectionUtils.isNotEmpty(beforeSelectedItems)
          && CollectionUtils.isNotEmpty(afterSelectedItems)) {
        if (!beforeSelectedItems.containsAll(afterSelectedItems)
            || !afterSelectedItems.containsAll(beforeSelectedItems)) {
          value =
              Arrays.toString(beforeSelectedItems.toArray())
                  + CHANGE_TO
                  + Arrays.toString(afterSelectedItems.toArray());
        }
      }
    }
    return value;
  }

  private static <E> String getFieldValue(Field field, E object) {
    String value = "";
    try {
      field.setAccessible(true);
      value = String.valueOf(field.get(object));
    } catch (IllegalAccessException e) {
      log.warn("Failed to tag ID of field " + field.getName(), e);
    }
    return value;
  }
}
