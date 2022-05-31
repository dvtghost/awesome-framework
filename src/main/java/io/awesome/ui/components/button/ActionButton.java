package io.awesome.ui.components.button;

import com.vaadin.flow.component.button.Button;
import io.awesome.ui.components.AbstractForm;
import io.awesome.ui.components.IFormAction;
import org.slf4j.Logger;

public interface ActionButton {
  Button getButton();

  String getType();

  void addActionHandler(IFormAction<?> handler, AbstractForm<?> form, Logger logger);

  default ActionButton setText(String text) {
    getButton().setText(text);
    return this;
  }
}
