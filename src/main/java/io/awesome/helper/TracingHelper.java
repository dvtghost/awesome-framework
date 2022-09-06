package io.awesome.helper;

import io.awesome.service.TracingService;
import io.awesome.ui.annotations.Trace;
import io.awesome.ui.annotations.TraceTag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class TracingHelper {

  private static final String EMPTY_ID = "empty";

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

  public static <E> void tracing(
      TracingService tracingService, String action, Consumer<E> changeValueFunc, E entity) {
    tracing(
        tracingService,
        action,
        e -> {
          changeValueFunc.accept(e);
          return null;
        },
        entity,
        null);
  }

  public static <E> void tracing(
      TracingService tracingService, String action, Function<E, E> changeValueFunc, E entity) {
    tracing(tracingService, action, changeValueFunc, entity, null);
  }

  public static <E> void tracing(
      TracingService tracingService,
      String action,
      Function<E, E> changeValueFunc,
      E entity,
      E snapshot) {
    Class<?> entityClazz = entity.getClass();
    for (Annotation annotation : entityClazz.getDeclaredAnnotations()) {
      if (annotation.annotationType() == Trace.class) {
        Trace trace = (Trace) annotation;
        final String operationName = trace.name() + "_" + action.toLowerCase();
        Map<String, String> tags = new HashMap<>();

        tracingService.trace(
                operationName,
            tags,
            () ->
                invokeChangeValueFunc(
                    changeValueFunc, entity, snapshot, entityClazz, trace, operationName));
        break;
      }
    }
  }

  private static <E> Map<String, String> invokeChangeValueFunc(
      Function<E, E> changeValueFunc,
      E entity,
      E snapshot,
      Class<?> entityClazz,
      Trace trace,
      String operationName) {
    final E before;
    if (snapshot == null) {
      before = TracingHelper.clone(entity);
    } else {
      before = snapshot;
    }
    E after = changeValueFunc.apply(entity);
    Map<String, String> changedValues = ObjectHelper.getDifference(before, after);
    changedValues.put("tracing_operation", operationName);
    for (Field field : entityClazz.getDeclaredFields()) {
      if (field.isAnnotationPresent(TraceTag.class)) {
        TraceTag traceTag = field.getAnnotation(TraceTag.class);
        if (traceTag.type() == TraceTag.Type.ID) {
          extractTraceTagID(trace, before, after, changedValues, field);
        } else if (traceTag.ignore()) {
          changedValues.remove(field.getName());
        }
      }
    }
    return changedValues;
  }

  private static <E> void extractTraceTagID(
      Trace trace, E before, E after, Map<String, String> changedValues, Field field) {
    if (after != null) {
      changedValues.computeIfAbsent(trace.name() + "_id", k -> getFieldValue(field, after));
    } else {
      changedValues.computeIfAbsent(trace.name() + "_id", k -> getFieldValue(field, before));
    }
  }

  private static <E> String getFieldValue(Field field, E object) {
    String value;
    try {
      field.setAccessible(true);
      value = String.valueOf(field.get(object));
    } catch (Exception e) {
      log.warn("Failed to tag ID of field " + field.getName(), e);
      value = EMPTY_ID;
    }
    return value;
  }
}
