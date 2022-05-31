package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.util.UIUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class WidgetField<T> extends AbstractFormElement<T, FormElement> {
    private ComponentEventListener<AttachEvent> valueChangeListener;

    public WidgetField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    public void setValueChangeListener(ComponentEventListener<AttachEvent> valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    @Override
    public Optional<Binder.Binding<T, ?>> binding(FormElement annotation, String fieldName) {
        try {
            Component field = (Component) util.invokeGetter(entity, fieldName);
            field.addAttachListener(this.valueChangeListener);
            FormLayout.FormItem item = this.parentForm.addFormItem(field, annotation.label());
            UIUtil.setColSpan(annotation.colspan(), item);
            items.put(fieldName, item);
            if (field instanceof HasValue) {
                log.info("rendering has value");
            }
        } catch (Exception e) {
            logError(entity, annotation, fieldName, e);
        }
        return Optional.empty();
    }




}
