package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.ValueProvider;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.util.UIUtil;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class DateTimeField<T> extends AbstractHasValueFormElement<T, LocalDateTime> {
    public DateTimeField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

//    @Override
//    public Optional<Binder.Binding<T, ?>> binding(FormElement annotation, String fieldName) {
//        try {
//            var dateTimeField = new DateTimePicker();
//            dateTimeField.setEnabled(annotation.enable());
//            dateTimeField.setRequiredIndicatorVisible(annotation.required());
//            dateTimeField.setLocale(Locale.UK);
//            dateTimeField.setWidthFull();
//            FormLayout.FormItem dateTimeFieldItem = this.parentForm.addFormItem(dateTimeField, annotation.label());
//            UIUtil.setColSpan(annotation.colspan(), dateTimeFieldItem);
//            items.put(fieldName, dateTimeFieldItem);
//            return Optional.of(binder
//                    .forField(dateTimeField)
//                    .bind(
//                            (ValueProvider<T, LocalDateTime>)
//                                    t ->  (LocalDateTime) util.invokeGetter(t, fieldName),
//                            (com.vaadin.flow.data.binder.Setter<T, LocalDateTime>)
//                                    (t, localDate) -> {
//                                        util.invokeSetter(t, fieldName, localDate);
//                                    }));
//
//        } catch (Exception e) {
//            logError(entity, annotation, fieldName, e);
//        }
//        return Optional.empty();
//    }

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
