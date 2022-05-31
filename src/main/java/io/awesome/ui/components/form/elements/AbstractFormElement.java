package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.util.SystemUtil;
import io.awesome.config.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public abstract class AbstractFormElement<T, A> {
    protected FormLayout parentForm;
    protected ExtendedBinder<T> binder;
    protected T entity;
    protected Map<String, FormLayout.FormItem> items;
    protected SystemUtil util = SystemUtil.getInstance();

    public AbstractFormElement(FormLayout parentForm,
                               ExtendedBinder<T> binder,
                               T entity,
                               Map<String, FormLayout.FormItem> items) {
        this.parentForm = parentForm;
        this.binder = binder;
        this.entity = entity;
        this.items = items;
    }

    protected void logError(T entity, FormElement annotation, String fieldName, Exception e) {
        log.error(
                Constants.EXCEPTION_PREFIX
                        + String.format(
                        " Error setupSelectField: fieldName %s, annotation %s, editEntity %s",
                        fieldName, annotation.toString(), entity.toString()),
                e);
    }

    public abstract Optional<Binder.Binding<T, ?>> binding(A annotation, String fieldName);
}
