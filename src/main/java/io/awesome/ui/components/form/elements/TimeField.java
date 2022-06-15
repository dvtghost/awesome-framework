package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.ValueProvider;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.util.UIUtil;

import java.time.LocalTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class TimeField<T> extends AbstractFormElement<T, LocalTime> {
    public TimeField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    public Optional<Binder.Binding<T, LocalTime>> binding(FormElement annotation, String fieldName) {
        try {
            var timeField = new TimePicker();
            timeField.setRequired(annotation.required());
            timeField.setEnabled(annotation.enable());
            timeField.setClearButtonVisible(annotation.isClearButton());
            timeField.setWidthFull();
            timeField.setLocale(Locale.UK);
            FormLayout.FormItem dateFieldItem = this.parentForm.addFormItem(timeField, annotation.label());
            UIUtil.setColSpan(annotation.colspan(), dateFieldItem);
            items.put(fieldName, dateFieldItem);
            return Optional.of(binder
                    .forField(timeField)
                    .bind(
                            (ValueProvider<T, LocalTime>) t -> (LocalTime) util.invokeGetter(t, fieldName),
                            (com.vaadin.flow.data.binder.Setter<T, LocalTime>)
                                    (t, localTime) -> {
                                        util.invokeSetter(t, fieldName, localTime);
                                    }));
        } catch (Exception e) {
            logError(entity, annotation, fieldName, e);
        }
        return Optional.empty();
    }
}
