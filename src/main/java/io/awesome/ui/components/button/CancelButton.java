package io.awesome.ui.components.button;

import com.vaadin.flow.component.button.Button;
import io.awesome.exception.SystemException;
import io.awesome.ui.components.AbstractForm;
import io.awesome.ui.components.IFormAction;
import io.awesome.config.Constants;
import org.slf4j.Logger;

public class CancelButton implements ActionButton {
  public static final String CANCEL_BTN_LABEL = "Cancel";
  public static final String TYPE_CANCEL = "Cancel";
  Button cancel = new Button(CANCEL_BTN_LABEL);

  public CancelButton() {
    cancel.setWidthFull();
    cancel.addClassNames("control-button", "blue-button");
  }

  @Override
  public void addActionHandler(IFormAction handler, AbstractForm form, Logger logger) {
    cancel.addClickListener(
        buttonClickEvent -> {
          try {
            handler.execute(form.getEntity());
          } catch (Exception e) {
            logger.error(Constants.EXCEPTION_PREFIX, e);
            throw new SystemException(e.getMessage(), e.getCause());
          }
        });
  }

  @Override
  public Button getButton() {
    return this.cancel;
  }

  @Override
  public String getType() {
    return TYPE_CANCEL;
  }
}
