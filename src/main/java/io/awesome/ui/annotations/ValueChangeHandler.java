package io.awesome.ui.annotations;

import com.vaadin.flow.component.formlayout.FormLayout;

import java.util.Map;

public interface ValueChangeHandler<T> {
  void trigger(
          String fieldName, Object value, T entity, Map<String, FormLayout.FormItem> formItems, FormLayout form);
}
