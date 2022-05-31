package io.awesome.ui.components.button;

import com.vaadin.flow.component.button.Button;
import io.awesome.exception.ValidateException;
import io.awesome.ui.components.AbstractForm;
import io.awesome.ui.components.IFormAction;
import io.awesome.util.FormUtil;
import io.awesome.config.Constants;
import org.slf4j.Logger;

public class SaveButton implements ActionButton {
  public static final String SAVE_DRAFT_BTN_LABEL = "Save Draft";
  public static final String SAVE_BTN_LABEL = "Save";
  public static final String TYPE_SAVE = "Save";
  Button button = new Button(SAVE_BTN_LABEL);

  public SaveButton() {
    button.setWidthFull();
    button.addClassNames("control-button", "blue-button");
  }

  @Override
  public void addActionHandler(IFormAction handler, AbstractForm form, Logger logger) {
    this.button.addClickListener(
        buttonClickEvent -> {
          try {
            Object entity = form.getEntity();
            form.getBinder().writeBean(entity);
            handler.execute(entity);
          } catch (ValidateException e) {
            var errors = e.getErrors();
            FormUtil.addError(errors, form);
            logger.error(Constants.VALIDATE_EXCEPTION_PREFIX, e);
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
    return TYPE_SAVE;
  }
}
