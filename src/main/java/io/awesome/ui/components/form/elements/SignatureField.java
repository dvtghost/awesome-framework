package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import de.f0rce.signaturepad.SignaturePad;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.util.UIUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Optional;

public class SignatureField<T> extends AbstractFormElement<T, FormElement> {
    public SignatureField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    public Optional<Binder.Binding<T, ?>> binding(FormElement annotation, String fieldName) {
        SignaturePad signature = new SignaturePad();
        signature.setHeight("150px");
        signature.setWidth("100%");
        signature.setBackgroundColor("white");
        signature.setPenColor("#000000");

        VerticalLayout wrapper = new VerticalLayout(signature);
        wrapper.setWidthFull();
        wrapper.setPadding(false);

        Button save = new Button("Finish");
        Button clear = UIUtil.createErrorPrimaryButton("Clear");

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidth("50%");
        buttonLayout.setPadding(true);

        String value = (String) util.invokeGetter(entity, fieldName);
        if (!StringUtils.isBlank(value)) {
            signature.setImage(value);
            buttonLayout.add(clear);
        } else {
            buttonLayout.add(save, clear);
        }

        save.setWidthFull();
        save.addClassNames("control-button", "blue-button");
        save.addClickListener(
                e -> {
                    if (!signature.isEmpty()) {
                        String data = signature.getImageURI();
                        if (!StringUtils.isBlank(data)) {
                            util.invokeSetter(entity, fieldName, data);
                            buttonLayout.removeAll();
                            buttonLayout.add(clear);
                        }
                    }
                });

        clear.setWidthFull();
        clear.addClassNames("control-button", "red-button");
        clear.addClickListener(
                e -> {
                    signature.clear();
                    buttonLayout.removeAll();
                    buttonLayout.add(save, clear);
                    util.invokeSetter(entity, fieldName, null);
                });

        wrapper.add(buttonLayout);

        FormLayout.FormItem signatureItems = this.parentForm.addFormItem(wrapper, annotation.label());
        UIUtil.setColSpan(annotation.colspan(), signatureItems);
        items.put(fieldName, signatureItems);
        return Optional.empty();
    }
}
