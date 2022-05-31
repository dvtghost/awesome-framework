package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.ValueProvider;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.util.UIUtil;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class DateField<T> extends AbstractFormElement<T, FormElement> {
    public DateField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    public Optional<Binder.Binding<T, ?>> binding(FormElement annotation, String fieldName) {
        try {
            var dateField = new DatePicker();
            dateField.setRequired(annotation.required());
            dateField.setEnabled(annotation.enable());
            dateField.setClearButtonVisible(annotation.isClearButton());
            dateField.setLocale(Locale.UK);
            dateField.setWidthFull();
            FormLayout.FormItem dateFieldItem = this.parentForm.addFormItem(dateField, annotation.label());
            UIUtil.setColSpan(annotation.colspan(), dateFieldItem);
            items.put(fieldName, dateFieldItem);
            return Optional.of(binder
                    .forField(dateField)
                    .bind(
                            (ValueProvider<T, LocalDate>)
                                    t -> {
                                        return (LocalDate) util.invokeGetter(t, fieldName);
                                    },
                            (com.vaadin.flow.data.binder.Setter<T, LocalDate>)
                                    (t, localDate) -> {
                                        util.invokeSetter(t, fieldName, localDate);
                                    }));
        } catch (Exception e) {
            logError(entity, annotation, fieldName, e);
        }
        return Optional.empty();
    }
}
