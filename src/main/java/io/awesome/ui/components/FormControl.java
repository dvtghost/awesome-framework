package io.awesome.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Style;
import io.awesome.ui.components.button.ActionButton;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class FormControl {
  private static final Logger logger = LoggerFactory.getLogger(FormControl.class);
  HorizontalLayout buttons = new HorizontalLayout();
  private AbstractForm form;
  private final Map<String, ActionButton> btnMap = new HashMap<>();

  public FormControl(AbstractForm form) {
    this.form = form;
    buttons.setWidthFull();
    buttons.getStyle().set("margin-top", "10px");
  }

  public FormControl addActionBtns(ActionButton... actionButtons) {
    Arrays.stream(actionButtons)
        .forEach(
            actionButton -> {
              Button button = actionButton.getButton();
              button.setVisible(false);
              btnMap.put(actionButton.getType(), actionButton);
            });
    return this;
  }

  public FormControl addComponent(Component component) {
    buttons.addComponentAsFirst(component);
    return this;
  }

  public Optional<ActionButton> getActionButton(String type) {
    return Optional.ofNullable(this.btnMap.get(type));
  }

  public void showActionButtons(String width, String... types) {
    this.btnMap
        .values()
        .forEach(
            actionButton -> {
              actionButton.getButton().setVisible(false);
            });
    buttons.removeAll();
    Arrays.stream(types)
        .forEach(
            type -> {
              if (this.btnMap.containsKey(type)) {
                Button button = this.btnMap.get(type).getButton();
                button.setVisible(true);
                buttons.add(button);
              }
            });
    this.buttons.setWidth(width);
  }

  public Style getStyle() {
    return this.buttons.getStyle();
  }
}
