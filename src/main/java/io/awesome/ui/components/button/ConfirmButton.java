package io.awesome.ui.components.button;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.awesome.exception.SystemException;
import io.awesome.ui.components.AbstractForm;
import io.awesome.ui.components.IFormAction;
import io.awesome.ui.components.dialog.ConfirmDialog;
import io.awesome.ui.util.UIUtil;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfirmButton implements ActionButton {
    public static final String TYPE_CONFIRM = "Confirm";
    private final Button button;
    private final ConfirmDialog dialog;

    public ConfirmButton(String label, String message, String... classNames) {
        button = UIUtil.createErrorPrimaryButton(label);
        dialog = new ConfirmDialog(label + " confirmation", message, classNames);
        buildButton(classNames);
    }

    private void buildButton(String... classNames) {
        List<String> classButton = new ArrayList<>(List.of("control-button"));
        if (classNames != null && classNames.length > 0) {
            Arrays.stream(classNames).forEach(classButton::add);
        } else {
            classButton.add("green-button");
        }
        this.button.addClassNames(classButton.toArray(new String[0]));
        this.button.setWidthFull();
        this.button.addClickListener(
                (event) -> {
                    this.dialog.open();
                });
        FlexLayout wrapper = new FlexLayout(this.button);
        wrapper.setWidthFull();
        wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
    }

    public void addActionHandler(IFormAction handler, AbstractForm form, Logger logger) {
        this.dialog.setConfirmListener(
                (buttonClickEvent) -> {
                    try {
                        handler.execute(form.getEntity());
                        this.dialog.close();
                    } catch (Exception e) {
                        logger.error("RUNTIME_EXCEPTION >>", e);
                        throw new SystemException(e.getMessage(), e.getCause());
                    }
                });
    }

    public Button getButton() {
        return this.button;
    }

    public String getType() {
        return TYPE_CONFIRM;
    }
}

