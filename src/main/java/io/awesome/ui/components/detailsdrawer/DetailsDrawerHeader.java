package io.awesome.ui.components.detailsdrawer;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.awesome.ui.components.FlexBoxLayout;
import io.awesome.ui.layout.size.Horizontal;
import io.awesome.ui.layout.size.Right;
import io.awesome.ui.layout.size.Vertical;
import io.awesome.ui.util.BoxShadowBorders;
import io.awesome.ui.util.UIUtil;

public class DetailsDrawerHeader extends FlexBoxLayout {

  private final Button close;
  private final Label title;

  public DetailsDrawerHeader(String title) {
    addClassName(BoxShadowBorders.BOTTOM);
    setFlexDirection(FlexDirection.COLUMN);
    setWidthFull();

    this.close = UIUtil.createTertiaryInlineButton(VaadinIcon.CLOSE);
    UIUtil.setLineHeight("1", this.close);

    this.title = UIUtil.createH4Label(title);

    FlexBoxLayout wrapper = new FlexBoxLayout(this.close, this.title);
    wrapper.setAlignItems(Alignment.CENTER);
    wrapper.setPadding(Horizontal.RESPONSIVE_L, Vertical.M);
    wrapper.setSpacing(Right.L);
    add(wrapper);
  }

  public DetailsDrawerHeader(String title, Component component) {
    this(title);
    add(component);
  }

  public void setTitle(String title) {
    this.title.setText(title);
  }

  public void addCloseListener(ComponentEventListener<ClickEvent<Button>> listener) {
    this.close.addClickListener(listener);
  }
}
