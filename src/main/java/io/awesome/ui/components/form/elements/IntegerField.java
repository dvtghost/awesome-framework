package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;

import java.util.Map;

public class IntegerField<T> extends AbstractHasValueFormElement<T, Integer> {

    public IntegerField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    protected HasValue<?, Integer> buildField(FormElement annotation, String fieldName) {
        com.vaadin.flow.component.textfield.IntegerField formComponent = new com.vaadin.flow.component.textfield.IntegerField();
        formComponent.setWidthFull();
        return formComponent;
    }
}
