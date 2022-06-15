package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneField<T> extends AbstractHasValueFormElement<T, String> {

    public PhoneField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    protected HasValue<?, String> buildField(FormElement annotation, String fieldName) {
        TextField component = new TextField();
        component.setWidthFull();
        return component;
    }

    @Override
    protected Binder.BindingBuilder<T, String> validate(FormElement annotation, Binder.BindingBuilder<T, String> bindingBuilder) {
        bindingBuilder = super.validate(annotation, bindingBuilder);
        if (StringUtils.isNotBlank(annotation.pattern())) {
            bindingBuilder = bindingBuilder
                    .withValidator(value -> isValidPattern(annotation.pattern(), value),
                            "Phone number is not valid. Eg: " + annotation.patternExample());
        }
        return bindingBuilder;
    }

    private static boolean isValidPattern(String pattern, String value) {
        Pattern ptrn = Pattern.compile(pattern);
        Matcher match = ptrn.matcher(value);
        return (match.find() && match.group().equals(value));
    }
}
