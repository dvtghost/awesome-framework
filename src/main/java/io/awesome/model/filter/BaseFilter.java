package io.awesome.model.filter;

import io.awesome.dto.FilterDto;
import io.awesome.enums.IAttributeEnum;
import io.awesome.mapper.TypedSet;
import io.awesome.util.SystemUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseFilter {
  private final String prefixOfClass;

  public static final String DATE_RANGE_SUFFIX_FROM = "RangeFrom";
  public static final String DATE_RANGE_SUFFIX_TO = "RangeTo";

  protected BaseFilter() {
    FilterPrefix classPrefixAnnotation = this.getClass().getAnnotation(FilterPrefix.class);
    this.prefixOfClass = classPrefixAnnotation != null ? classPrefixAnnotation.prefix() : "";
  }

  public List<FilterDto> toFilters() {
    List<FilterDto> results =
        Arrays.stream(this.getClass().getDeclaredFields())
            .map(this::mapFieldToFilterDto)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    results = prepareDateRangeFilters(results);
    results.addAll(customFilters());
    return results;
  }

  private FilterDto mapFieldToFilterDto(Field field) {
    FilterPrefix prefixAnnotation = field.getAnnotation(FilterPrefix.class);
    String prefix = prefixAnnotation != null ? prefixAnnotation.prefix() : this.prefixOfClass;
    prefix = !StringUtils.isBlank(prefix) ? prefix + "##" : "";

    FilterFieldName fieldNameAnnotation = field.getAnnotation(FilterFieldName.class);
    String fieldName =
        (fieldNameAnnotation != null && !StringUtils.isBlank(fieldNameAnnotation.name()))
            ? fieldNameAnnotation.name()
            : field.getName();
    fieldName = prefix + fieldName;
    Object value = SystemUtil.getInstance().invokeGetter(this, field.getName());
    if (value instanceof IAttributeEnum) {
      return new FilterDto(
          fieldName,
          FilterDto.Operator.IN,
          new Object[] {((IAttributeEnum<?, ?>) value).getValue()});
    }
    if (value instanceof TypedSet) {
      TypedSet<? extends Enum<?>> collection = (TypedSet<? extends Enum<?>>) value;
      if (!CollectionUtils.isEmpty(collection)) {
        return new FilterDto(
            fieldName,
            FilterDto.Operator.IN,
            collection.stream()
                .map(IAttributeEnum.class::cast)
                .map(IAttributeEnum::getValue)
                .toArray());
      }
      return null;
    } else if (value instanceof Collection) {
      if (!CollectionUtils.isEmpty((Collection<?>) value)) {
        return new FilterDto(fieldName, FilterDto.Operator.IN, ((Collection<?>) value).toArray());
      }
      return null;
    }
    if (value instanceof String && !StringUtils.isBlank((String) value))
      return new FilterDto(fieldName, FilterDto.Operator.LIKE, new Object[] {value});
    if (value instanceof Integer
        || value instanceof Long
        || value instanceof LocalDate
        || value instanceof LocalDateTime) {
      return new FilterDto(fieldName, FilterDto.Operator.EQ, new Object[] {value});
    }
    return null;
  }

  @NotNull
  private List<FilterDto> prepareDateRangeFilters(final List<FilterDto> filters) {
    List<FilterDto> createdDateFilters =
        filters.stream()
            .filter(
                filterDto ->
                    filterDto.getField().endsWith(DATE_RANGE_SUFFIX_FROM)
                        || filterDto.getField().endsWith(DATE_RANGE_SUFFIX_TO))
            .collect(Collectors.toList());
    final List<FilterDto> results =
        filters.stream()
            .filter(
                filterDto ->
                    !filterDto.getField().endsWith(DATE_RANGE_SUFFIX_FROM)
                        && !filterDto.getField().endsWith(DATE_RANGE_SUFFIX_TO))
            .collect(Collectors.toList());

    Map<String, Pair<FilterDto, FilterDto>> fieldToDateRangeFilters = new HashMap<>();
    createdDateFilters.forEach(
        filterDto -> {
          String fieldName =
              filterDto
                  .getField()
                  .replace(DATE_RANGE_SUFFIX_FROM, "")
                  .replace(DATE_RANGE_SUFFIX_TO, "");
          Pair<FilterDto, FilterDto> pair =
              fieldToDateRangeFilters.getOrDefault(fieldName, Pair.of(null, null));
          if (filterDto.getField().endsWith(DATE_RANGE_SUFFIX_TO)) {
            fieldToDateRangeFilters.put(fieldName, Pair.of(pair.getLeft(), filterDto));
          } else {
            fieldToDateRangeFilters.put(fieldName, Pair.of(filterDto, pair.getRight()));
          }
        });

    fieldToDateRangeFilters.forEach(
        (fieldName, pair) -> {
          if (pair.getLeft() != null && pair.getRight() != null) {
            FilterDto from = pair.getLeft();
            FilterDto to = pair.getRight();
            Object[] range = new Object[2];
            range[0] = from.getValue()[0];
            range[1] = to.getValue()[0];
            results.add(new FilterDto(fieldName, FilterDto.Operator.BETWEEN, range));
          } else {
            if (pair.getLeft() != null) {
              results.add(
                  new FilterDto(fieldName, FilterDto.Operator.AFTER, pair.getLeft().getValue()));
            } else if (pair.getRight() != null) {
              results.add(
                  new FilterDto(fieldName, FilterDto.Operator.BEFORE, pair.getRight().getValue()));
            }
          }
        });

    return results;
  }

  protected Collection<FilterDto> customFilters() {
    return new ArrayList<>();
  }
}
