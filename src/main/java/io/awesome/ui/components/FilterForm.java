package io.awesome.ui.components;

import com.vaadin.flow.data.binder.Binder;
import io.awesome.ui.annotations.ValueChangeHandler;
import io.awesome.ui.annotations.ValueInitHandler;
import io.awesome.ui.binder.ExtendedBinder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class FilterForm<E extends BaseFilterUI> extends AbstractForm<E> {
  FilterControl filterControl;
  Class<E> baseFilterClass;
  Binder<E> binder;
  E entity;
  Set<String> savedFilters = new HashSet<>();

  public FilterForm(
      Class<E> entityClazz,
      E entity,
      ExtendedBinder<E> binder,
      Set<String> filterNames,
      ValueChangeHandler<E> onChange,
      ValueInitHandler<E> onInit,
      FilterControl<E> filterControl) {
    super(entityClazz, entity, binder, true, onChange, onInit, "");
    this.entity = entity;
    this.binder = binder;
    this.savedFilters.add("Save as New Filter");
    this.savedFilters.addAll(filterNames);
    this.filterControl = filterControl;
    this.add(this.filterControl);
  }
}
