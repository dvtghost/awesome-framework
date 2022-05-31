package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.ValueProvider;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.enums.FormElementType;
import io.awesome.ui.util.UIUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TextField<T> extends  AbstractFormElement<T, FormElement> {
    public TextField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    public Optional<Binder.Binding<T, ?>> binding(FormElement annotation, String fieldName) {
        try {
            if (annotation.type().equals(FormElementType.PasswordField)) {
                PasswordField formComponent = new PasswordField();
                formComponent.setWidthFull();
                formComponent.setClearButtonVisible(annotation.isClearButton());
                formComponent.setRequired(annotation.required());
                formComponent.setEnabled(annotation.enable());
                var formItem = this.parentForm.addFormItem(formComponent, annotation.label());
                UIUtil.setColSpan(annotation.colspan(), formItem);
                items.put(fieldName, formItem);
                return Optional.of(binder
                        .forField(formComponent)
                        .bind(
                                (ValueProvider<T, String>) t -> (String) util.invokeGetter(t, fieldName),
                                (com.vaadin.flow.data.binder.Setter<T, String>)
                                        (t, s) -> {
                                            util.invokeSetter(t, fieldName, s);
                                        }));
            } else if (annotation.type().equals(FormElementType.TextAreaField)) {
                TextArea formComponent = new TextArea();
                formComponent.setWidthFull();
                formComponent.setClearButtonVisible(annotation.isClearButton());
                formComponent.setRequired(annotation.required());
                formComponent.setEnabled(annotation.enable());
                var formItem = this.parentForm.addFormItem(formComponent, annotation.label());
                UIUtil.setColSpan(annotation.colspan(), formItem);
                items.put(fieldName, formItem);
                return Optional.of(binder
                        .forField(formComponent)
                        .bind(
                                (ValueProvider<T, String>) t -> (String) util.invokeGetter(t, fieldName),
                                (com.vaadin.flow.data.binder.Setter<T, String>)
                                        (t, s) -> {
                                            util.invokeSetter(t, fieldName, s);
                                        }));
            } else if (annotation.type().equals(FormElementType.IntegerField)) {
                IntegerField formComponent = new IntegerField();
                formComponent.setWidthFull();
                formComponent.setClearButtonVisible(annotation.isClearButton());
                formComponent.setRequiredIndicatorVisible(annotation.required());
                formComponent.setEnabled(annotation.enable());
                var formItem = this.parentForm.addFormItem(formComponent, annotation.label());
                UIUtil.setColSpan(annotation.colspan(), formItem);
                items.put(fieldName, formItem);
                return Optional.of(binder
                        .forField(formComponent)
                        .bind(
                                (ValueProvider<T, Integer>) t -> (Integer) util.invokeGetter(t, fieldName),
                                (com.vaadin.flow.data.binder.Setter<T, Integer>)
                                        (t, s) -> {
                                            util.invokeSetter(t, fieldName, s);
                                        }));
            } else if (annotation.type().equals(FormElementType.LongField)) {
                com.vaadin.flow.component.textfield.TextField formComponent = new com.vaadin.flow.component.textfield.TextField();
                formComponent.setWidthFull();
                formComponent.setClearButtonVisible(annotation.isClearButton());
                formComponent.setPattern("\\d*");
                formComponent.setPreventInvalidInput(true);
                formComponent.setRequiredIndicatorVisible(annotation.required());
                formComponent.setEnabled(annotation.enable());
                var formItem = this.parentForm.addFormItem(formComponent, annotation.label());
                UIUtil.setColSpan(annotation.colspan(), formItem);
                items.put(fieldName, formItem);
                return Optional.of(binder
                        .forField(formComponent)
                        .bind(
                                (ValueProvider<T, String>)
                                        t -> {
                                            Long value = (Long) util.invokeGetter(t, fieldName);
                                            if (Objects.isNull(value)) {
                                                return "";
                                            }
                                            return String.valueOf(value);
                                        },
                                (com.vaadin.flow.data.binder.Setter<T, String>)
                                        (t, s) -> {
                                            if (StringUtils.isBlank(s)) {
                                                util.invokeSetter(t, fieldName, null);
                                            } else {
                                                util.invokeSetter(t, fieldName, Long.parseLong(s));
                                            }
                                        }));
            } else if (annotation.type().equals(FormElementType.EmailField)) {
                EmailField formComponent = new EmailField();
                formComponent.setWidthFull();
                formComponent.setClearButtonVisible(annotation.isClearButton());
                formComponent.setRequiredIndicatorVisible(annotation.required());
                formComponent.setEnabled(annotation.enable());
                var formItem = this.parentForm.addFormItem(formComponent, annotation.label());
                UIUtil.setColSpan(annotation.colspan(), formItem);
                items.put(fieldName, formItem);
                return Optional.of(binder
                        .forField(formComponent)
                        .bind(
                                (ValueProvider<T, String>) t -> (String) util.invokeGetter(t, fieldName),
                                (com.vaadin.flow.data.binder.Setter<T, String>)
                                        (t, s) -> {
                                            util.invokeSetter(t, fieldName, s);
                                        }));

            } else if (annotation.type().equals(FormElementType.DoubleField)) {
                NumberField formComponent = new NumberField();
                formComponent.setWidthFull();
                formComponent.setClearButtonVisible(annotation.isClearButton());
                formComponent.setRequiredIndicatorVisible(annotation.required());
                formComponent.setEnabled(annotation.enable());
                var formItem = this.parentForm.addFormItem(formComponent, annotation.label());
                UIUtil.setColSpan(annotation.colspan(), formItem);
                items.put(fieldName, formItem);
                return Optional.of(binder
                        .forField(formComponent)
                        .bind(
                                (ValueProvider<T, Double>) t -> (Double) util.invokeGetter(t, fieldName),
                                (com.vaadin.flow.data.binder.Setter<T, Double>)
                                        (t, s) -> {
                                            util.invokeSetter(t, fieldName, s);
                                        }));
            } else {
                com.vaadin.flow.component.textfield.TextField formComponent = new com.vaadin.flow.component.textfield.TextField();
                formComponent.setWidthFull();
                formComponent.setRequired(annotation.required());
                formComponent.setEnabled(annotation.enable());
                formComponent.setClearButtonVisible(annotation.isClearButton());
                var formItem = this.parentForm.addFormItem(formComponent, annotation.label());
                UIUtil.setColSpan(annotation.colspan(), formItem);
                items.put(fieldName, formItem);
                return Optional.of(binder
                        .forField(formComponent)
                        .bind(
                                (ValueProvider<T, String>) t -> (String) util.invokeGetter(t, fieldName),
                                (com.vaadin.flow.data.binder.Setter<T, String>)
                                        (t, s) -> {
                                            util.invokeSetter(t, fieldName, s);
                                        }));
            }
        } catch (Exception e) {
            logError(entity, annotation, fieldName, e);
        }
        return Optional.empty();
    }
}
