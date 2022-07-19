package io.awesome.ui.components.dialog;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfirmDialog extends Dialog {

  private String title = "";
  private String message = "";
  private HorizontalLayout header;
  private VerticalLayout body;
  private HorizontalLayout footer;
  private Button confirmButton;

  public ConfirmDialog(String... classNames) {
    VerticalLayout dialogLayout = createDialogLayout(classNames);
    this.add(dialogLayout);
  }

  public ConfirmDialog(String title, String message, String... classNames) {
    this(classNames);
    this.setTitle(title);
    this.setMessage(message);
  }

  private VerticalLayout createDialogLayout(String... classNames) {
    this.header = buildHeaderLayout();
    this.body = buildBodyLayout();
    this.footer = buildFooterLayout(classNames);

    VerticalLayout dialogLayout = new VerticalLayout(header, body, footer);
    dialogLayout.setPadding(false);
    dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
    dialogLayout.getStyle().set("width", "600px").set("max-width", "100%");

    return dialogLayout;
  }

  private HorizontalLayout buildHeaderLayout() {
    H2 title = new H2(this.title);
    title.setClassName("title");
    title.getElement().setAttribute("title", this.title);
    title
        .getStyle()
        .set("margin", "-5px 0 0 15px")
        .set("font-size", "22px")
        .set("font-weight", "bold");
    HorizontalLayout headerLayout = new HorizontalLayout(title);
    headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
    return headerLayout;
  }

  private HorizontalLayout buildFooterLayout(String... classNames) {
    confirmButton = new Button("Confirm");
    List<String> classButton = new ArrayList<>(List.of("control-button"));
    if (classNames != null && classNames.length > 0) {
      Arrays.stream(classNames).forEach(classButton::add);
    } else {
      classButton.add("green-button");
    }
    confirmButton.addClassNames(classButton.toArray(new String[0]));
    Button cancelButton = new Button("Cancel", e -> this.close());
    cancelButton.addClassNames("control-button", "blue-button");

    HorizontalLayout footerLayout = new HorizontalLayout(confirmButton, cancelButton);
    footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
    footerLayout.getStyle().set("margin", "20px 0 -15px 0");
    return footerLayout;
  }

  private VerticalLayout buildBodyLayout() {
    Span messageField = new Span(this.message);
    messageField.getElement().setAttribute("message", message);
    VerticalLayout bodyLayout = new VerticalLayout(messageField);
    bodyLayout.setSpacing(false);
    bodyLayout.setPadding(false);
    bodyLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
    return bodyLayout;
  }

  public void setConfirmListener(ComponentEventListener<ClickEvent<Button>> confirmListener) {
    this.confirmButton.addClickListener(confirmListener);
  }

  public void setTitle(String title) {
    this.title = title;
    this.header
        .getElement()
        .getChildren()
        .filter(element -> element.hasAttribute("title"))
        .findFirst()
        .ifPresent(
            element -> {
              element.setText(title);
              element.setAttribute("title", title);
            });
  }

  public void setMessage(String message) {
    this.message = message;
    this.body
        .getElement()
        .getChildren()
        .filter(element -> element.hasAttribute("message"))
        .findFirst()
        .ifPresent(
            element -> {
              element.setText(message);
              element.setAttribute("message", message);
            });
  }

  public HorizontalLayout getHeader() {
    return header;
  }

  public VerticalLayout getBody() {
    return body;
  }

  public HorizontalLayout getFooter() {
    return footer;
  }
}
