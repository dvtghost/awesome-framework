package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.util.UIUtil;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractHasValueFormElement<T, V> extends AbstractFormElement<T, V>  {

    public AbstractHasValueFormElement(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    public Optional<Binder.Binding<T, V>> binding(FormElement annotation, String fieldName) {
        try {
            HasValue<?, V> field = buildField(annotation, fieldName);
            if (field instanceof HasEnabled) {
                ((HasEnabled) field).setEnabled(annotation.enable());
            }
            field.setRequiredIndicatorVisible(annotation.required());
            field.setReadOnly(annotation.readOnly());

            FormLayout.FormItem formItem = buildFormItem(annotation, (Component) field);

            this.parentForm.add(formItem);
            items.put(fieldName, formItem);

            Binder.BindingBuilder<T, V> bindingBuilder = binder.forField(field);
            bindingBuilder = validate(annotation, bindingBuilder);


            return Optional.of(bind(bindingBuilder, fieldName));
        } catch (Exception e) {
            logError(entity, annotation, fieldName, e);
        }
        return Optional.empty();
    }

    protected Binder.BindingBuilder<T,V> validate(FormElement annotation, Binder.BindingBuilder<T,V> bindingBuilder){
        if (annotation.required()) {
            bindingBuilder = bindingBuilder.asRequired("This field is required");
        }
        return bindingBuilder;
    }

    protected FormLayout.FormItem buildFormItem(FormElement annotation, Component field) {
        FormLayout.FormItem formItem = new FormLayout.FormItem(field);
        Component label;
        if (annotation.required()) {
            label = requiredLabel(annotation.label());
        } else {
            label = label(annotation.label());
        }
        formItem.getElement().appendChild(label.getElement());

        UIUtil.setColSpan(annotation.colspan(), formItem);
        return formItem;
    }

    private static Component requiredLabel(String text) {
        String indicatorText = "(*)";
        Label label = new Label(text);
        label.add(new Html("<sup style='color: red'>" + indicatorText + "</sup>"));
        label.getElement().setAttribute("slot", "label");
        return label;
    }

    private static Component label(String text) {
        Label label = new Label(text);
        label.getElement().setAttribute("slot", "label");
        return label;
    }

    protected  Binder.Binding<T, V> bind(Binder.BindingBuilder<T, V> bindingBuilder, String fieldName) {
        return bindingBuilder.bind(bindingValueProvider(fieldName),
                bindingSetter(fieldName));
    }

    protected abstract HasValue<?, V> buildField(FormElement annotation, String fieldName);

    protected ValueProvider<T, V> bindingValueProvider(String fieldName) {
        return t -> (V) util.invokeGetter(entity, fieldName);
    }

    protected Setter<T, V> bindingSetter(String fieldName) {
        return (t, v) -> util.invokeSetter(t, fieldName, v);
    }
}
