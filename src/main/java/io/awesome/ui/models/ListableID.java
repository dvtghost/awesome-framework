package io.awesome.ui.models;

public interface ListableID<E extends EditableID<ID>, ID> {
  void setEntity(E entity);
}
