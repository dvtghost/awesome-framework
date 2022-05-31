package io.awesome.util;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.awesome.dto.ErrorDto;
import io.awesome.dto.ErrorsDto;
import io.awesome.ui.components.AbstractForm;
import io.awesome.config.Constants;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FormUtil {
  private static final Map<String, FormLayout.FormItem> formItemMap = new HashMap<>();

  public static void addError(ErrorsDto errors, AbstractForm form) {
    formItemMap.clear();
    for (var error : errors.getErrors()) {
      var errorField = error.getErrorField();
      if (StringUtils.isNotBlank(errorField)) {
        var formField = (FormLayout.FormItem) form.getFormItems().get(errorField);
        if (formField != null) {
          formField.setId(UUID.randomUUID().toString().replace("-", ""));
          removeError(formField);
          if (!errorField.equals(Constants.GENERAL_ERROR_FIELD)) {
            FormUtil.addErrorToField(error, formField);
          } else {
            formField.setVisible(true);
            FormUtil.addErrorToBottom(error, formField);
          }
          formItemMap.put(errorField, formField);
        }
      }
    }
  }

  public static void removeError(FormLayout.FormItem formField) {
    var old =
        formField
            .getChildren()
            .filter(
                i -> {
                  var c = i.getElement().getComponent();
                  if (c.isPresent() && c.get() instanceof Span) {
                    var component = c.get();
                    return component.getElement() != null
                        && component.getElement().getAttribute("id") != null
                        && component.getElement().getAttribute("id").equals("error-message");
                  }
                  return false;
                })
            .findFirst();
    old.ifPresent(formField::remove);
    Optional<String> formFieldId = formField.getId();
    UI.getCurrent()
        .getPage()
        .executeJs(
            "const element = document.getElementById('"
                + formFieldId.orElse("")
                + "');"
                + changeInputColor("var(--lumo-contrast-10pct)", ""));
    formField.removeClassName("error");
  }

  public static void addErrorToBottom(ErrorDto error, FormLayout.FormItem formField) {
    formField.addClassNames("error");
    formField.removeAll();
    String[] messages = error.getErrorKey().split(ErrorDto.TO_STRING_DELIM);

    Div errorField = new Div();
    errorField.setWidth("97%");

    Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE));
    closeButton.setWidth("3%");
    closeButton.getStyle().set("color", "var(--lumo-error-text-color)");
    closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

    HorizontalLayout layout = new HorizontalLayout(errorField, closeButton);
    layout.setWidthFull();
    layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    layout.setAlignItems(FlexComponent.Alignment.CENTER);
    layout.getStyle().set("flex-wrap", "nowrap");
    closeButton.addClickListener(
        event -> {
          formField.setVisible(false);
          for (FormLayout.FormItem ff : formItemMap.values()) {
            removeError(ff);
          }
        });

    for (String message : messages) {
      Span errorMessage = new Span();
      errorMessage.setClassName("error-message");
      errorMessage.setText(message);
      errorField.add(errorMessage);
    }

    formField.add(layout);
  }

  public static void addErrorToField(ErrorDto error, FormLayout.FormItem formField) {
    formField.addClassName("error");
    Span errorMessage = new Span();
    errorMessage.setId("error-message");
    errorMessage.setClassName("error-message");
    errorMessage.setText(error.getErrorKey());
    formField.add(errorMessage);
    UI.getCurrent()
        .getPage()
        .executeJs(
            "document.querySelectorAll('.error').forEach((element) => {"
                + changeInputColor("var(--lumo-error-color-10pct)", "var(--lumo-error-text-color)")
                + "});");
  }

  public static String changeInputColor(String color, String textColor) {
    return "if (element) {"
        + "  const loop = (element) => {"
        + "    const COLOR = '"
        + color
        + "';"
        + "    const TEXT_COLOR = '"
        + textColor
        + "';"
        + "    const PART_FIELD = ['input-field'];"
        + "    if (element.part) {"
        + "      if (PART_FIELD.includes(element.part[0])) {"
        + "        element.style.backgroundColor = COLOR;"
        + "      }"
        + "      if (element.part[0] === 'label') {"
        + "        element.style.color = TEXT_COLOR;"
        + "      }"
        + "    }"
        + "    if (element.shadowRoot) {"
        + "      const shadows = element.shadowRoot.childNodes;"
        + "      shadows.forEach((shadow) => loop(shadow));"
        + "    }"
        + "    const childs = element.childNodes;"
        + "    childs.forEach((child) => loop(child));"
        + "  };"
        + "  loop(element);"
        + "}";
  }
}
