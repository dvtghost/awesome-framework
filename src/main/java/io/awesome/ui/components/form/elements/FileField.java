package io.awesome.ui.components.form.elements;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.data.binder.Binder;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.components.FlexBoxLayout;
import io.awesome.ui.util.UIUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Optional;

public class FileField<T> extends AbstractFormElement<T, FormElement>{
    public FileField(FormLayout parentForm, ExtendedBinder<T> binder, T entity, Map<String, FormLayout.FormItem> items) {
        super(parentForm, binder, entity, items);
    }

    @Override
    public Optional<Binder.Binding<T, ?>> binding(FormElement annotation, String fieldName) {
        FlexBoxLayout wrapper = new FlexBoxLayout();
        wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        wrapper.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        wrapper.setWidthFull();

        FileBuffer buffer = new FileBuffer();
        Upload upload = new Upload(buffer);
        upload.setWidth("68%");

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/jpg", "application/pdf");
        if (StringUtils.isNotBlank(annotation.acceptedFileTypes())) {
            String[] acceptedFileTypes = annotation.acceptedFileTypes().split(",");
            upload.setAcceptedFileTypes(acceptedFileTypes);
        }

        upload.setMaxFileSize(10485760); // 10MB
        Div output = new Div();
        output.setWidth("30%");
        wrapper.add(upload, output);

        upload.addAllFinishedListener(
                e -> {
                    // actions when upload finished
                    Notification.show("Upload Successfully").setPosition(Notification.Position.TOP_END);
                });

        upload.addFileRejectedListener(
                event -> {
                    // actions when upload failed
                    output.removeAll();
                    Notification.show(event.getErrorMessage()).setPosition(Notification.Position.TOP_END);
                });

        FormLayout.FormItem uploadItem = this.parentForm.addFormItem(wrapper, annotation.label());
        UIUtil.setColSpan(annotation.colspan(), uploadItem);
        items.put(fieldName, uploadItem);
        // TODO Dang add binding
        return Optional.empty();
    }
}
