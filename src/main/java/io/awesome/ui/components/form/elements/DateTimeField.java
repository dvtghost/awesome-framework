package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

public class DateTimeField<T> extends AbstractHasValueFormElement<T, LocalDateTime> {
    public DateTimeField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    protected HasValue<?, LocalDateTime> buildField(FormElement annotation, String fieldName) {
        var dateTimeField = new DateTimePicker();
        dateTimeField.setEnabled(annotation.enable());
        dateTimeField.setRequiredIndicatorVisible(annotation.required());
        dateTimeField.setLocale(Locale.UK);
        dateTimeField.setWidthFull();
        return dateTimeField;
    }


}
