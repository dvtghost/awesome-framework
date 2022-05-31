package io.awesome.mapper;

import java.util.HashSet;

public class TypedSet<T extends Enum<T>> extends HashSet<T> {
  private Class<T> componentType;

  public TypedSet() {
    super();
  }

  public TypedSet(Class<T> clazz) {
    super();
    this.componentType = clazz;
  }

  public Class<?> getComponentType() {
    return this.componentType;
  }

  public Enum<T>[] getAllPossibleItemValues() {
    return this.componentType.getEnumConstants();
  }
}
