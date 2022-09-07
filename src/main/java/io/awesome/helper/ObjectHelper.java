package io.awesome.helper;

import io.awesome.exception.SystemException;
import io.awesome.ui.components.SelectDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ObjectHelper {

  private static final String CHANGE_TO = " → ";

  public static Map<String, String> getDifference(Object before, Object after) {
    Map<String, String> values = new HashMap<>();
    if (before == null || after == null) {
      return values;
    }
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
          if (valueBefore instanceof SelectDto) {
            String diff = getDifferenceForSelectDto(valueBefore, valueAfter);
            if (StringUtils.isNotBlank(diff)) {
              values.put(field.getName(), diff);
            }
          } else {
            values.put(field.getName(), valueBefore + CHANGE_TO + valueAfter);
          }
        }
      } catch (IllegalAccessException e) {
        throw new SystemException("Cannot detect the different between two objects", e);
      }
    }
    return values;
  }

  private static String getDifferenceForSelectDto(Object before, Object after) {
    String value = null;
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
    return value;
  }
}
