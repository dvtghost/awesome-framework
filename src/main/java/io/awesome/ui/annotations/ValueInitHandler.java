package io.awesome.ui.annotations;

import com.vaadin.flow.component.formlayout.FormLayout;

import java.util.Map;

public interface ValueInitHandler<T> {
  void trigger(T entity, Map<String, FormLayout.FormItem> formItems);
}
