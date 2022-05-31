package io.awesome.enums;

public interface IAttributeEnum<X, T> {

  X getValue();

  T getEnum(X value);

  String getLabel();

  String getName();

  default String getColor() {
    return null;
  }
}
