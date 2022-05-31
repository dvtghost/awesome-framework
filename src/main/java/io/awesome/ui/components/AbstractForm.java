package io.awesome.ui.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.annotations.FormGroupElement;
import io.awesome.ui.annotations.ValueChangeHandler;
import io.awesome.ui.annotations.ValueInitHandler;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.components.form.elements.*;
import io.awesome.ui.components.form.elements.TextField;
import io.awesome.ui.enums.FormElementType;
import io.awesome.ui.util.LumoStyles;
import io.awesome.ui.util.UIUtil;
import io.awesome.util.FormUtil;
import io.awesome.util.SystemUtil;
import io.awesome.config.Constants;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.util.*;
import java.util.function.Supplier;

@Slf4j
@CssImport("./styles/components/form.css")
public class AbstractForm<T> extends FormLayout {
  public static final String FORM_HEADER = "form-header";
  private static final String UPLOAD_FOLDER = "upload";
  @Getter protected final ExtendedBinder<T> binder;
  private final ValueChangeHandler<T> onChange;
  private final ValueInitHandler<T> onInit;
  @Getter private final VerticalLayout formControlWrapper;
  @Getter @Setter protected T entity;
  @Getter @Setter protected Class<T> beanType;
  @Getter @Setter protected String root;
  @Getter @Setter protected Map<String, Boolean> expandCollapses;
  @Getter @Setter private FlexBoxLayout formContainer;
  @Getter @Setter private Map<String, FormItem> formItems;
  @Getter @Setter private Map<String, AbstractForm> formGroups;
  @Getter private FormControl formControl;
  Map<FormElementType, AbstractFormElement<T, FormElement>> formElements;

  public AbstractForm(
      Class<T> clazz,
      T entity,
      ExtendedBinder<T> binder,
      Boolean isAbleToEdit,
      ValueChangeHandler<T> onChange,
      ValueInitHandler<T> onInit,
      String root) {
    super();
    this.entity = entity;
    this.beanType = clazz;
    this.binder = binder;
    this.onChange = onChange;
    this.onInit = onInit;
    this.root = StringUtils.isBlank(root) ? "" : root;
    this.expandCollapses = new HashMap<>();

    this.formItems = new HashMap<>();
    this.formGroups = new HashMap<>();
    this.formItems.putAll(setupForm(entity));
    this.onInit.trigger(entity, this.formItems);
    addClassName("abstract-form-container");

    this.formControlWrapper = new VerticalLayout();
    UIUtil.setColSpan(2, formControlWrapper);
    formControlWrapper.setPadding(false);
    add(this.formControlWrapper);

    if (!isAbleToEdit) {
      formControlWrapper.setVisible(false);
      for (Map.Entry<String, FormItem> item : this.formItems.entrySet()) {
        this.formItems.get(item.getKey()).setEnabled(false);
      }
    }

  }

  @Override
  public void onAttach(AttachEvent event) {
    addClassNames(LumoStyles.Padding.Horizontal.M);
    setResponsiveSteps(
        new ResponsiveStep("0", 1, ResponsiveStep.LabelsPosition.TOP),
        new ResponsiveStep("600px", 2, ResponsiveStep.LabelsPosition.TOP));
  }

  private Map<String, FormItem> setupForm(T entity) {
    this.formElements = new HashMap<>();
    Map<String, FormItem> items = new HashMap<>();
    for (var field : getBeanType().getDeclaredFields()) {
      if (field.isAnnotationPresent(FormElement.class)) {
        var annotation = field.getAnnotation(FormElement.class);
        var fieldName = StringUtils.isBlank(root) ? field.getName() : root + "." + field.getName();
        var util = SystemUtil.getInstance();

        if (!annotation.formSectionHeader().isEmpty()) {
          HorizontalLayout head = UIUtil.createFormSectionTitle(annotation.formSectionHeader());
          head.addClassNames(FORM_HEADER);
          add(head);
        }
        Optional<AbstractFormElement<T, FormElement>> formElement;
        switch (annotation.type()) {
          case SelectField:
            formElement = getFormElement(annotation,
                    () -> new SelectField<>(this, binder, entity, items ));
            break;
          case MultiSelectField:
            formElement = getFormElement(annotation,
                    () -> new MultiSelectField<>(this, binder, entity, items ));
            break;
          case CheckboxGroup:
            formElement = getFormElement(annotation,
                    () -> new CheckboxGroup<>(this, binder, entity, items ));
            break;
          case DateField:
          case DateRangePicker:
            formElement = getFormElement(annotation,
                    () -> new DateField<>(this, binder, entity, items ));
          case DateTimeField:
            formElement = getFormElement(annotation,
                    () -> new DateTimeField<>(this, binder, entity, items ));
            break;
          case TimeField:
            formElement = getFormElement(annotation,
                    () -> new TimeField<>(this, binder, entity, items ));
            break;
          case FileField:
            formElement = getFormElement(annotation,
                    () -> new FileField<>(this, binder, entity, items ));
            break;
          case RadioButtonGroup:
            formElement = getFormElement(annotation,
                    () -> new RadioButtonGroup<>(this, binder, entity, items ));
            break;
          case Checkbox:
            formElement = getFormElement(annotation,
                    () -> new Checkbox<>(this, binder, entity, items ));
            break;
          case SignatureField:
            formElement = getFormElement(annotation,
                    () -> new SignatureField<>(this, binder, entity, items ));
            break;
          case Widget:
            WidgetField<T> widgetField = (WidgetField<T>) getFormElement(annotation,
                    () -> new WidgetField<>(this, binder, entity, items )).get();
            widgetField.setValueChangeListener(event -> this.onChange.trigger(fieldName, field, entity, formItems, this));
            formElement = Optional.of(widgetField);
            break;
          case PasswordField:
          default:
            formElement = getFormElement(annotation,
                    () -> new TextField<>(this, binder, entity, items ));
        }

        formElement.flatMap(fe -> fe.binding(annotation, fieldName)).ifPresent(b -> {
          this.binder.addBinding(fieldName, annotation.type(), b);
          b.getField()
                  .addValueChangeListener(
                          valueChangeEvent -> {
                            this.onChange.trigger(
                                    fieldName, valueChangeEvent.getValue(), entity, formItems, this);
                          });
        });
        // Remove error when focus FormItem
        var formField = items.get(fieldName);
        formField.addClickListener(
            event -> {
              FormUtil.removeError(formField);
              var errorField = items.get(Constants.GENERAL_ERROR_FIELD);
              errorField.setVisible(false);
            });
      } else if (field.isAnnotationPresent(FormGroupElement.class)) {
        var annotation = field.getAnnotation(FormGroupElement.class);
        var fieldName = StringUtils.isBlank(root) ? field.getName() : root + "." + field.getName();
        var util = SystemUtil.getInstance();
        var obj = util.invokeGetter(entity, fieldName);
        if (obj == null) continue;
        try {
          formContainer = new FlexBoxLayout();
          formContainer.setWidthFull();
          formContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

          expandCollapses.put(fieldName, true);

          VerticalLayout groupWrapper = new VerticalLayout();
          groupWrapper.addClassName("form-group");

          AbstractForm formGroup =
              new AbstractForm(
                  field.getType(),
                  entity,
                  binder,
                  true,
                  onChange,
                  (e, formItemsMap) -> {
                    this.formItems.putAll(formItemsMap);
                    FormItem[] formItems = new FormItem[formItemsMap.size()];
                    formItemsMap.values().toArray(formItems);
                    if (!annotation.editable()) {
                      for (FormItem item : formItems) {
                        item.setEnabled(false);
                      }
                    }
                  },
                  fieldName);
          formGroups.put(field.getName(), formGroup);
          formGroup.addClassNames("no-padding-left", "no-padding-right");
          formGroup.getStyle().set("display", "none");

          HorizontalLayout wrapper = new HorizontalLayout();
          wrapper.setWidthFull();
          wrapper.addClassNames("head-label");
          groupWrapper.add(wrapper);

          if (annotation.isCollapse()) {
            formGroup.getStyle().set("display", "none");

            Icon icon = new Icon(VaadinIcon.CHEVRON_CIRCLE_DOWN_O);
            icon.addClassName("head-icon");
            wrapper.add(icon);

            if (annotation.isDefaultCollapse()) {
              expandCollapses.put(fieldName, false);
              icon.addClassName("head-icon-turn");
              formGroup.getStyle().set("display", "block");
            }

            wrapper.addClickListener(
                e -> {
                  boolean temp = expandCollapses.get(fieldName);
                  expandCollapses.put(fieldName, !temp);
                  if (!expandCollapses.get(fieldName)) {
                    icon.addClassName("head-icon-turn");
                    formGroup.getStyle().set("display", "block");
                  } else {
                    icon.removeClassName("head-icon-turn");
                    formGroup.getStyle().set("display", "none");
                  }
                });
          } else {
            formGroup.getStyle().set("display", "block");
          }

          if (StringUtils.isNotBlank(annotation.label())) {
            Span span = new Span();
            span.setText(annotation.label());
            wrapper.add(span);

            if (annotation.isFormEdit()) {
              formContainer.addClassName("form-container");
              span.getStyle().set("color", "white");
              wrapper.getStyle().set("background", "#293C58");
              wrapper.addClassNames("abstract-head-label");
              groupWrapper.addClassNames("abstract-form-group");
            }
          } else {
            wrapper.setVisible(false);
            formGroup.getStyle().set("margin", "0");
            groupWrapper.setPadding(false);
          }

          groupWrapper.add(formGroup);

          formContainer.add(groupWrapper);
          UIUtil.setColSpan(annotation.colspan(), formContainer);
          add(formContainer);
        } catch (Exception e) {
          log.error(Constants.EXCEPTION_PREFIX, e);
        }
      }
      var errorItem = addFormItem(new Div(), "");
      errorItem.addClassName("error-bottom-item");
      errorItem.setVisible(false);
      items.put(Constants.GENERAL_ERROR_FIELD, errorItem);
      UIUtil.setColSpan(2, items.values().toArray(new FormItem[0]));
    }
    return items;
  }

  private Optional<AbstractFormElement<T, FormElement>> getFormElement(FormElement annotation,
                                                                       Supplier<AbstractFormElement<T, FormElement>> initFormElement)  {
    AbstractFormElement<T, FormElement> formElement;
    FormElementType formElementType = annotation.type();
    if (formElements.containsKey(formElementType)) {
      formElement = formElements.get(formElementType);
    } else {
      formElement = initFormElement.get();
      formElements.put(formElementType, formElement);
    }
    return Optional.of(formElement);
  }

  public void updateFormView(List<String> fieldNames) {
    binder.updateBinding(fieldNames, entity);
  }

  private Component createComponent(String mimeType, String fileName, InputStream stream) {
    FlexBoxLayout wrapper = new FlexBoxLayout();
    wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
    wrapper.setAlignItems(FlexComponent.Alignment.CENTER);
    wrapper.setSizeFull();
    if (mimeType.startsWith("image")) {
      Image image = new Image();
      try {
        byte[] bytes = IOUtils.toByteArray(stream);
        image
            .getElement()
            .setAttribute(
                "src", new StreamResource(fileName, () -> new ByteArrayInputStream(bytes)));
        try (ImageInputStream in =
            ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
          final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
          if (readers.hasNext()) {
            ImageReader reader = readers.next();
            try {
              reader.setInput(in);
              image.setWidth(reader.getWidth(0) + "px");
              image.setHeight(reader.getHeight(0) + "px");
              image.getStyle().set("display", "inherit");
            } finally {
              reader.dispose();
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      image.setSizeFull();
      wrapper.add(image);
    } else {
      Icon icon = new Icon(VaadinIcon.FILE_TEXT_O);
      icon.setSize("50%");
      wrapper.add(icon);
    }
    return wrapper;
  }

  public void setFormControl(FormControl formControl) {
    formControlWrapper.removeAll();
    this.formControl = formControl;
    formControlWrapper.add(this.formControl.getButtons());
    formControlWrapper.setPadding(true);
  }

  private void logError(FormElement annotation, String fieldName, Exception e) {
    log.error(
        Constants.EXCEPTION_PREFIX
            + String.format(
                " Error setupSelectField: fieldName %s, annotation %s",
                fieldName, annotation.toString()),
        e);
  }

  private void logError(T entity, FormElement annotation, String fieldName, Exception e) {
    log.error(
        Constants.EXCEPTION_PREFIX
            + String.format(
                " Error setupSelectField: fieldName %s, annotation %s, editEntity %s",
                fieldName, annotation.toString(), entity.toString()),
        e);
  }
}
