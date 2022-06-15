package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;


public class DateField<T> extends AbstractHasValueFormElement<T, LocalDate> {
    public DateField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    protected HasValue<?, LocalDate> buildField(FormElement annotation, String fieldName) {
        var dateField = new DatePicker();
        dateField.setRequired(annotation.required());
        dateField.setEnabled(annotation.enable());
        dateField.setClearButtonVisible(annotation.isClearButton());
        dateField.setLocale(Locale.UK);
        dateField.setWidthFull();
        return dateField;
    }

}
