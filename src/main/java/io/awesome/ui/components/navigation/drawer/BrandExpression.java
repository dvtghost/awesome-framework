package io.awesome.ui.components.navigation.drawer;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import io.awesome.ui.util.UIUtil;

@CssImport("./styles/components/brand-expression.css")
public class BrandExpression extends Div {

  private final String CLASS_NAME = "brand-expression";

  private final Image logo;
  private final Label title;

  public BrandExpression(String text) {
    setClassName(CLASS_NAME);

    logo = new Image(UIUtil.IMG_PATH + "home.jpeg", text);
    logo.setAlt(text + " logo");
    logo.setClassName(CLASS_NAME + "__logo");

    title = UIUtil.createH3Label(text);
    title.addClassName(CLASS_NAME + "__title");

    add(logo, title);
  }
}
