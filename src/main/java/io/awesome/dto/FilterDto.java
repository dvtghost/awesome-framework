package io.awesome.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class FilterDto {

  private String field;
  private Operator operator;
  private Object[] value;
  private List<FilterDto> filters;

  public FilterDto() {
    this(null, Operator.EQ, null);
  }

  public FilterDto(String field, Object[] value) {
    this(field, Operator.EQ, value);
  }

  public FilterDto(String field, Operator op, Object[] value) {
    this.field = field;
    this.operator = op;
    this.value = value;
  }

  public FilterDto(Operator op, List<FilterDto> filters) {
    this.operator = op;
    this.filters = filters;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder
        .append("FilterDto [field=")
        .append(field)
        .append(", op=")
        .append(operator)
        .append(", value=")
        .append(Arrays.toString(value))
        .append("]");
    return builder.toString();
  }

  public enum Operator {
    EQ,
    NE,
    LT,
    LE,
    GT,
    GE,
    LIKE,
    ILIKE,
    BETWEEN,
    IN,
    ISNOTNULL,
    ISNULL,
    BEFORE,
    AFTER,
    OR,
    AND
  }
}
