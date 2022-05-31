package io.awesome.ui.components;

import com.vaadin.flow.component.html.Span;
import io.awesome.ui.util.UIUtil;
import io.awesome.ui.util.css.lumo.BadgeColor;
import io.awesome.ui.util.css.lumo.BadgeShape;
import io.awesome.ui.util.css.lumo.BadgeSize;

import java.util.StringJoiner;

public class Badge extends Span {

  public Badge(String text) {
    this(text, BadgeColor.NORMAL);
  }

  public Badge(String text, BadgeColor color) {
    super(text);
    UIUtil.setTheme(color.getThemeName(), this);
  }

  public Badge(String text, BadgeColor color, BadgeSize size, BadgeShape shape) {
    super(text);
    StringJoiner joiner = new StringJoiner(" ");
    joiner.add(color.getThemeName());
    if (shape.equals(BadgeShape.PILL)) {
      joiner.add(shape.getThemeName());
    }
    if (size.equals(BadgeSize.S)) {
      joiner.add(size.getThemeName());
    }
    UIUtil.setTheme(joiner.toString(), this);
  }
}
