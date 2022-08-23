package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;

import java.util.Map;

public class IntegerField<T> extends AbstractHasValueFormElement<T, Integer> {

  public IntegerField(
      FormLayout parentForm,
      ExtendedBinder<T> binder,
      T entity,
      Map<String, FormLayout.FormItem> items) {
    super(parentForm, binder, entity, items);
  }

  @Override
  protected HasValue<?, Integer> buildField(FormElement annotation, String fieldName) {
    com.vaadin.flow.component.textfield.IntegerField formComponent =
        new com.vaadin.flow.component.textfield.IntegerField();
    formComponent.setWidthFull();
    return formComponent;
  }

  @Override
  protected Binder.BindingBuilder<T, Integer> validate(
      FormElement annotation, Binder.BindingBuilder<T, Integer> bindingBuilder) {
    bindingBuilder = super.validate(annotation, bindingBuilder);
    if (annotation.min() != Long.MIN_VALUE) {
      bindingBuilder.withValidator(
          value -> value >= annotation.min(), "Must be greater than or equal to " + annotation.min());
    }

    if (annotation.max() != Long.MAX_VALUE) {
      bindingBuilder.withValidator(
          value -> value <= annotation.max(), "Must be less than or equal to " + annotation.max());
    }

    return bindingBuilder;
  }
}
