package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Objects;

public class LongField<T> extends AbstractHasValueFormElement<T, String> {

  public LongField(
      FormLayout parentForm,
      ExtendedBinder<T> binder,
      T entity,
      Map<String, FormLayout.FormItem> items) {
    super(parentForm, binder, entity, items);
  }

  @Override
  protected HasValue<?, String> buildField(FormElement annotation, String fieldName) {
    com.vaadin.flow.component.textfield.TextField formComponent =
        new com.vaadin.flow.component.textfield.TextField();
    formComponent.setWidthFull();
    formComponent.setClearButtonVisible(annotation.isClearButton());
    formComponent.setPattern("\\d*");
    formComponent.setPreventInvalidInput(true);
    formComponent.setRequiredIndicatorVisible(annotation.required());
    formComponent.setEnabled(annotation.enable());
    return formComponent;
  }

  @Override
  protected ValueProvider<T, String> bindingValueProvider(String fieldName) {
    return t -> {
      Long value = (Long) util.invokeGetter(t, fieldName);
      if (Objects.isNull(value)) {
        return "";
      }
      return String.valueOf(value);
    };
  }

  @Override
  protected Setter<T, String> bindingSetter(String fieldName) {
    return (t, s) -> {
      if (StringUtils.isBlank(s)) {
        util.invokeSetter(t, fieldName, null);
      } else {
        util.invokeSetter(t, fieldName, Long.parseLong(s));
      }
    };
  }
}
