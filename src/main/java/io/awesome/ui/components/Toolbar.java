package io.awesome.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.awesome.ui.layout.size.Horizontal;
import io.awesome.ui.layout.size.Top;
import io.awesome.ui.util.UIUtil;
import io.awesome.ui.util.css.BoxSizing;
import io.awesome.ui.views.Callback;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor
public class Toolbar<E> extends FlexBoxLayout implements IEventListener {
  private static final Logger logger = LoggerFactory.getLogger(Toolbar.class);
  @Setter @Getter private E entity;
  private Callback callback;

  public Toolbar(String buttonTitle, Boolean canCreate) {
    if (!canCreate) {
      return;
    }
    if (buttonTitle == null) {
      buttonTitle = "New";
    }
    Button newButton = createButton(buttonTitle);
    newButton.addClickListener(event -> this.callback.trigger(event));
    setBoxSizing(BoxSizing.BORDER_BOX);
    setHeight("80");
    setPadding(Horizontal.RESPONSIVE_X, Top.M);
    getStyle().set("padding-left", "0px !important");
    UIUtil.setColSpan(2, this);
    this.add(newButton);
  }

  public void addMoreButton(String title, Callback callback) {
    Button button = createButton(title);
    button.addClickListener(callback::trigger);
    this.add(button);
  }

  public void addMoreComponent(Component... components) {
    this.add(components);
  }

  @NotNull
  public Button createButton(String buttonTitle) {
    Button newButton = new Button(buttonTitle, new Icon(VaadinIcon.PLUS));
    newButton.getStyle().set("height", "auto");
    newButton.getStyle().set("padding", "5px 10px");
    newButton.getStyle().set("background", "#122b40");
    newButton.getStyle().set("border-color", "#122b40");
    newButton.getStyle().set("color", "white");
    newButton.getStyle().set("margin-right", "20px");
    return newButton;
  }

  @Override
  public void addEventListener(Callback callback) {
    this.callback = callback;
  }
}
