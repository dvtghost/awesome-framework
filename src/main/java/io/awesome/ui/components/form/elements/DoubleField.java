package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.NumberField;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;

import java.util.Map;

public class DoubleField<T> extends AbstractHasValueFormElement<T, Double> {

    public DoubleField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    protected HasValue<?, Double> buildField(FormElement annotation, String fieldName) {
        NumberField formComponent = new NumberField();
        formComponent.setWidthFull();;
        return formComponent;
    }
}
