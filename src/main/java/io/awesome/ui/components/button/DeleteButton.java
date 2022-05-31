package io.awesome.ui.components.button;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.awesome.config.Constants;
import io.awesome.ui.components.AbstractForm;
import io.awesome.ui.components.IFormAction;
import io.awesome.ui.util.UIUtil;
import org.slf4j.Logger;

public class DeleteButton implements ActionButton {
  public static final String DELETE_BTN_LABEL = "Delete";
  public static final String TYPE_DELETE = "Delete";
  Button button = UIUtil.createErrorPrimaryButton(DELETE_BTN_LABEL);
  Button confirmButton = UIUtil.createErrorPrimaryButton("Confirm");
  Dialog dialog = new Dialog();

  public DeleteButton() {
    button.addClassNames("control-button", "red-button");
    button.setWidthFull();
    button.addClickListener(
        event -> {
          dialog.removeAll();
          dialog.setWidth("400px");
          dialog.setCloseOnOutsideClick(false);

          Span message = new Span();
          message.setText(
              "Are you sure you want to " + this.button.getText().toLowerCase() + " this record ?");
          message.setSizeFull();

          confirmButton.addClassNames("control-button", "red-button");
          Button cancelButton = new Button("Cancel", e -> dialog.close());
          cancelButton.addClassNames("control-button", "blue-button");
          HorizontalLayout flexLayout = new HorizontalLayout(confirmButton, cancelButton);
          flexLayout.setWidthFull();
          flexLayout.setMargin(true);
          flexLayout.setPadding(true);
          dialog.add(message, flexLayout);
          dialog.open();
        });
    FlexLayout wrapper = new FlexLayout(button);
    wrapper.setWidthFull();
    wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
  }

  @Override
  public void addActionHandler(IFormAction handler, AbstractForm form, Logger logger) {
    this.confirmButton.addClickListener(
        buttonClickEvent -> {
          try {
            handler.execute(form.getEntity());
            dialog.close();
          } catch (Exception e) {
            logger.error(Constants.EXCEPTION_PREFIX, e);
          }
        });
  }

  @Override
  public Button getButton() {
    return button;
  }

  @Override
  public String getType() {
    return TYPE_DELETE;
  }
}
