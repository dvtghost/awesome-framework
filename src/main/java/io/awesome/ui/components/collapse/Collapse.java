package io.awesome.ui.components.collapse;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

@CssImport("./styles/components/collapse.css")
public class Collapse extends VerticalLayout {
  private final HorizontalLayout header;
  private final Div body;
  private final boolean isDefaultCollapse;
  private final String title;
  private final boolean isCollapse;
  private boolean expandCollapse;
  private Span detailTitle;
  private Component headerControl;

  private Collapse(Builder builder) {
    setPadding(false);
    setMargin(false);
    this.header = new HorizontalLayout();
    this.body = new Div();
    this.title = builder.title;
    this.isCollapse = builder.isCollapse;
    this.isDefaultCollapse = builder.isDefaultCollapse;
    this.expandCollapse = !isDefaultCollapse;
    this.headerControl = builder.headerControl;
    createContent(builder.components);
  }

  public static Builder newBuilder() {
    return Builder.create();
  }

  private void createContent(Component... content) {
    header.setWidthFull();
    header.addClassNames("collapse-head-label");

    body.setSizeFull();
    body.addClassName("collapse-body");
    body.add(content);

    if (isCollapse) {
      body.getStyle().set("display", "none");

      Icon icon = new Icon(VaadinIcon.CHEVRON_CIRCLE_DOWN_O);
      icon.addClassName("head-icon");
      header.add(icon);

      if (isDefaultCollapse) {
        expandCollapse = false;
        icon.addClassName("head-icon-turn");
        body.getStyle().set("display", "block");
      }

      header.addClickListener(
          e -> {
            expandCollapse = !expandCollapse;
            if (!expandCollapse) {
              icon.addClassName("head-icon-turn");
              body.getStyle().set("display", "block");
            } else {
              icon.removeClassName("head-icon-turn");
              body.getStyle().set("display", "none");
            }
          });
    } else {
      body.getStyle().set("display", "block");
    }


    detailTitle = new Span();
    detailTitle.setText(title);
    detailTitle.getStyle().set("color", "white");
    if (headerControl != null) {
      HorizontalLayout titleLayout = new HorizontalLayout();
      titleLayout.getStyle().set("background", "#293C58");
      titleLayout.add(detailTitle);
      titleLayout.setWidth("50%");

      HorizontalLayout controlLayout = new HorizontalLayout();
      controlLayout.add(headerControl);
      controlLayout.setJustifyContentMode(JustifyContentMode.END);
      controlLayout.setWidth("50%");

      header.add(titleLayout, controlLayout);
      header.setWidthFull();
    } else {

      header.getStyle().set("background", "#293C58");
      header.add(detailTitle);
    }

    add(header, body);
  }

  public void setDetailTitle(String title) {
    if (detailTitle != null) {
      detailTitle.setText(title);
    }
  }

  @NoArgsConstructor
  public static class Builder {
    private String title;
    private Component headerControl;
    private boolean isDefaultCollapse;
    private boolean isCollapse;
    private Component[] components;

    private static Builder create() {
      return new Builder();
    }

    public Builder setTitle(String title) {
      if (StringUtils.isBlank(title)) {
        setTitle("Default Collapse Label");
      } else {
        this.title = title;
      }
      return this;
    }

    public Builder setHeaderControl(Component headerControl) {
      this.headerControl = headerControl;
      return this;
    }

    public Builder setIsCollapse(boolean isCollapse) {
      this.isCollapse = isCollapse;
      return this;
    }

    public Builder setIsDefaultCollapse(boolean isDefaultCollapse) {
      this.isDefaultCollapse = isDefaultCollapse;
      return this;
    }

    public Builder setComponents(Component... components) {
      this.components = components;

      if (components == null || components.length == 0) {
        this.components = new Component[] {};
      }
      return this;
    }

    public Collapse build() {
      return new Collapse(this);
    }
  }
}
