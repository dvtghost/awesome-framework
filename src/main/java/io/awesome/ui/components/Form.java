package io.awesome.ui.components;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.MessageDigestUtil;
import com.vaadin.flow.server.StreamResource;
import io.awesome.ui.annotations.FormElement;
import io.awesome.ui.annotations.ValueChangeHandler;
import io.awesome.ui.annotations.ValueInitHandler;
import io.awesome.ui.enums.FormElementType;
import io.awesome.ui.util.LumoStyles;
import io.awesome.ui.util.UIUtil;
import io.awesome.util.SystemUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.vaadin.gatanaso.MultiselectComboBox;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@CssImport("./styles/components/form.css")
public class Form<T> extends FormLayout {
  public static final String FORM_HEADER = "form-header";
  @Getter private final Binder<T> binder;
  private final IFormAction<T> onValidate;
  private final IFormAction<T> onSave;
  private final IFormAction<T> onCancel;
  private final IFormAction<T> onDelete;
  private final ValueChangeHandler<T> onChange;
  private final ValueInitHandler<T> onInit;
  private final boolean editable;
  @Getter @Setter String deleteBtnName = "Delete";
  @Getter @Setter private T entity;
  @Getter @Setter private Class<T> beanType;

  public Form(
      Class<T> clazz,
      T entity,
      Binder<T> binder,
      boolean editable,
      IFormAction<T> onValidate,
      IFormAction<T> onSave,
      IFormAction<T> onDelete,
      IFormAction<T> onCancel,
      ValueChangeHandler<T> onChange,
      ValueInitHandler<T> onInit) {
    super();
    this.entity = entity;
    this.beanType = clazz;
    this.binder = binder;
    this.onValidate = onValidate;
    this.onDelete = onDelete;
    this.onSave = onSave;
    this.onCancel = onCancel;
    this.onChange = onChange;
    this.onInit = onInit;
    this.editable = editable;
  }

  @Override
  public void onAttach(AttachEvent event) {
    addClassNames(LumoStyles.Padding.Horizontal.M);
    setResponsiveSteps(
        new ResponsiveStep("0", 1, ResponsiveStep.LabelsPosition.TOP),
        new ResponsiveStep("600px", 2, ResponsiveStep.LabelsPosition.TOP),
        new ResponsiveStep("800px", 3, ResponsiveStep.LabelsPosition.TOP));

    var items = setupForm(entity);
    if (editable) {
      add(setupButtons(this.entity));
    } else {
      for (FormItem item : items.values()) {
        item.setEnabled(false);
      }
    }
    onInit.trigger(entity, items);
  }

  private Map<String, FormItem> setupForm(T entity) {
    Map<String, FormItem> items = new HashMap<>();
    for (var field : getBeanType().getDeclaredFields()) {
      if (field.isAnnotationPresent(FormElement.class)) {
        var annotation = field.getAnnotation(FormElement.class);
        var fieldName = field.getName();
        var util = SystemUtil.getInstance();
        if (!annotation.formSectionHeader().isEmpty()) {
          HorizontalLayout head = UIUtil.createFormSectionTitle(annotation.formSectionHeader());
          head.addClassNames(FORM_HEADER);
          add(head);
        }

        switch (annotation.type()) {
          case SelectField:
            try {
              setupSelectField(entity, items, annotation, fieldName, util);
            } catch (Exception e) {
              log.error(e.getMessage());
            }
            break;
          case MultiSelectField:
            setupMultiSelectField(entity, items, annotation, fieldName, util);
            break;
          case DateField:
            setupDateField(items, annotation, fieldName, util, false);
            break;
          case TimeField:
            setupDateField(items, annotation, fieldName, util, true);
            break;
          case FileField:
            setupUploadField(entity, items, annotation, fieldName, util);
            break;
          case Checkbox:
            setupCheckboxField(entity, items, annotation, fieldName, util);
            break;
          case Widget:
            setupWidgetField(entity, items, annotation, fieldName, util);
            break;
          case PasswordField:
          default:
            setupTextField(items, annotation, fieldName, util);
        }
      }
    }

    UIUtil.setColSpan(2, items.values().toArray(new FormItem[0]));
    return items;
  }

  private void setupCheckboxField(
      T entity,
      Map<String, FormItem> items,
      FormElement annotation,
      String fieldName,
      SystemUtil util) {
    var checkbox = new Checkbox();
    checkbox.setWidthFull();
    FormItem dateFieldItem = addFormItem(checkbox, annotation.label());
    items.put(fieldName, dateFieldItem);
    binder
        .forField(checkbox)
        .bind(
            (ValueProvider<T, Boolean>) t -> (boolean) util.invokeGetter(entity, fieldName),
            (com.vaadin.flow.data.binder.Setter<T, Boolean>)
                (t, value) -> {
                  util.invokeSetter(entity, fieldName, value);
                  onChange.trigger(fieldName, value, entity, items, this);
                });
  }

  private void setupWidgetField(
      T entity,
      Map<String, FormItem> items,
      FormElement annotation,
      String fieldName,
      SystemUtil util) {
    Component field = (Component) util.invokeGetter(entity, fieldName);
    FormItem item = addFormItem(field, annotation.label());
    items.put(fieldName, item);

    if (field instanceof HasValue) {
      log.info("rendering has value");
    }
  }

  private void setupMultiSelectField(
      T editEntity,
      Map<String, FormItem> items,
      FormElement annotation,
      String fieldName,
      SystemUtil util) {
    MultiselectComboBox<SelectDto.SelectItem> select = new MultiselectComboBox<>();
    select.setWidthFull();
    select.setRequired(annotation.required());
    select.setEnabled(annotation.enable());
    SelectDto dto = (SelectDto) util.invokeGetter(editEntity, fieldName);
    select.setItems(((SelectDto) util.invokeGetter(editEntity, fieldName)).getItems());
    List<SelectDto.SelectItem> selected = dto.getSelected();
    binder
        .forField(select)
        .bind(
            (ValueProvider<T, Set<SelectDto.SelectItem>>)
                t -> {
                  if (selected != null && !selected.isEmpty()) return new HashSet<>(selected);
                  return null;
                },
            (com.vaadin.flow.data.binder.Setter<T, Set<SelectDto.SelectItem>>)
                (t, a) -> {
                  List<SelectDto.SelectItem> newSelected = new ArrayList<>(a);
                  dto.setSelected(newSelected);
                  util.invokeSetter(editEntity, fieldName, dto);
                  onChange.trigger(fieldName, dto, entity, items, this);
                });

    select.setItemLabelGenerator(SelectDto.SelectItem::getLabel);

    FormItem selectItem = addFormItem(select, annotation.label());
    items.put(fieldName, selectItem);
  }

  private void setupSelectField(
      T editEntity,
      Map<String, FormItem> items,
      FormElement annotation,
      String fieldName,
      SystemUtil util) {
    ComboBox<SelectDto.SelectItem> select = new ComboBox<>();
    select.setWidthFull();
    select.setRequired(annotation.required());
    select.setEnabled(annotation.enable());
    select.setClearButtonVisible(false);
    SelectDto dto = (SelectDto) util.invokeGetter(editEntity, fieldName);
    select.setItems(dto.getItems());
    List<SelectDto.SelectItem> selected = dto.getSelected();

    binder
        .forField(select)
        .bind(
            (ValueProvider<T, SelectDto.SelectItem>)
                t -> {
                  if (selected != null && !selected.isEmpty()) return selected.get(0);
                  return null;
                },
            (com.vaadin.flow.data.binder.Setter<T, SelectDto.SelectItem>)
                (t, a) -> {
                  List<SelectDto.SelectItem> newSelected = new ArrayList<>();
                  newSelected.add(a);
                  dto.setSelected(newSelected);
                  util.invokeSetter(editEntity, fieldName, dto);
                  onChange.trigger(fieldName, dto, entity, items, this);
                });
    select.setItemLabelGenerator(SelectDto.SelectItem::getLabel);
    FormItem selectItem = addFormItem(select, annotation.label());
    items.put(fieldName, selectItem);
  }

  private void setupDateField(
      Map<String, FormItem> items,
      FormElement annotation,
      String fieldName,
      SystemUtil util,
      boolean isTime) {
    if (!isTime) {
      var dateField = new DatePicker();
      dateField.setRequired(annotation.required());
      dateField.setEnabled(annotation.enable());
      dateField.setLocale(Locale.UK);
      FormItem dateFieldItem = addFormItem(dateField, annotation.label());
      items.put(fieldName, dateFieldItem);
      binder
          .forField(dateField)
          .bind(
              (ValueProvider<T, LocalDate>) t -> (LocalDate) util.invokeGetter(t, fieldName),
              (com.vaadin.flow.data.binder.Setter<T, LocalDate>)
                  (t, localDate) -> {
                    util.invokeSetter(t, fieldName, localDate);
                    onChange.trigger(fieldName, localDate, entity, items, this);
                  });
      dateField.setWidthFull();

    } else {
      var timeField = new TimePicker();
      timeField.setRequired(annotation.required());
      timeField.setEnabled(annotation.enable());
      timeField.setClearButtonVisible(true);
      FormItem dateFieldItem = addFormItem(timeField, annotation.label());
      items.put(fieldName, dateFieldItem);
      binder
          .forField(timeField)
          .bind(
              (ValueProvider<T, LocalTime>) t -> (LocalTime) util.invokeGetter(t, fieldName),
              (com.vaadin.flow.data.binder.Setter<T, LocalTime>)
                  (t, localTime) -> {
                    util.invokeSetter(t, fieldName, localTime);
                    onChange.trigger(fieldName, localTime, entity, items, this);
                  });
      timeField.setWidthFull();
    }
  }

  private void setupTextField(
      Map<String, FormItem> items, FormElement annotation, String fieldName, SystemUtil util) {
    if (annotation.type().equals(FormElementType.PasswordField)) {
      PasswordField formComponent = new PasswordField();
      formComponent.setWidthFull();
      formComponent.setClearButtonVisible(true);
      formComponent.setRequired(annotation.required());
      formComponent.setEnabled(annotation.enable());
      var formItem = addFormItem(formComponent, annotation.label());
      items.put(fieldName, formItem);
      binder
          .forField(formComponent)
          .bind(
              (ValueProvider<T, String>) t -> (String) util.invokeGetter(t, fieldName),
              (com.vaadin.flow.data.binder.Setter<T, String>)
                  (t, s) -> {
                    util.invokeSetter(t, fieldName, s);
                    onChange.trigger(fieldName, s, entity, items, this);
                  });

    } else if (annotation.type().equals(FormElementType.TextAreaField)) {
      TextArea formComponent = new TextArea();
      formComponent.setWidthFull();
      formComponent.setClearButtonVisible(true);
      formComponent.setRequired(annotation.required());
      formComponent.setEnabled(annotation.enable());
      var formItem = addFormItem(formComponent, annotation.label());
      items.put(fieldName, formItem);
      binder
          .forField(formComponent)
          .bind(
              (ValueProvider<T, String>) t -> (String) util.invokeGetter(t, fieldName),
              (com.vaadin.flow.data.binder.Setter<T, String>)
                  (t, s) -> {
                    util.invokeSetter(t, fieldName, s);
                    onChange.trigger(fieldName, s, entity, items, this);
                  });
    } else if (annotation.type().equals(FormElementType.IntegerField)) {
      IntegerField formComponent = new IntegerField();
      formComponent.setWidthFull();
      formComponent.setClearButtonVisible(true);
      formComponent.setRequiredIndicatorVisible(annotation.required());
      formComponent.setEnabled(annotation.enable());
      var formItem = addFormItem(formComponent, annotation.label());
      items.put(fieldName, formItem);
      binder
          .forField(formComponent)
          .bind(
              (ValueProvider<T, Integer>) t -> (Integer) util.invokeGetter(t, fieldName),
              (com.vaadin.flow.data.binder.Setter<T, Integer>)
                  (t, s) -> {
                    util.invokeSetter(t, fieldName, s);
                    onChange.trigger(fieldName, s, entity, items, this);
                  });
    } else if (annotation.type().equals(FormElementType.DoubleField)) {
      NumberField formComponent = new NumberField();
      formComponent.setWidthFull();
      formComponent.setClearButtonVisible(true);
      formComponent.setRequiredIndicatorVisible(annotation.required());
      formComponent.setEnabled(annotation.enable());
      var formItem = addFormItem(formComponent, annotation.label());
      items.put(fieldName, formItem);
      binder
          .forField(formComponent)
          .bind(
              (ValueProvider<T, Double>) t -> (Double) util.invokeGetter(t, fieldName),
              (com.vaadin.flow.data.binder.Setter<T, Double>)
                  (t, s) -> {
                    util.invokeSetter(t, fieldName, s);
                    onChange.trigger(fieldName, s, entity, items, this);
                  });
    } else if (annotation.type().equals(FormElementType.EmailField)) {
      EmailField formComponent = new EmailField();
      formComponent.setWidthFull();
      formComponent.setClearButtonVisible(true);
      formComponent.setRequiredIndicatorVisible(annotation.required());
      formComponent.setEnabled(annotation.enable());
      var formItem = addFormItem(formComponent, annotation.label());
      items.put(fieldName, formItem);
      binder
          .forField(formComponent)
          .bind(
              (ValueProvider<T, String>) t -> (String) util.invokeGetter(t, fieldName),
              (com.vaadin.flow.data.binder.Setter<T, String>)
                  (t, s) -> {
                    util.invokeSetter(t, fieldName, s);
                    onChange.trigger(fieldName, s, entity, items, this);
                  });

    } else {
      TextField formComponent = new TextField();
      formComponent.setWidthFull();
      formComponent.setClearButtonVisible(true);
      formComponent.setRequired(annotation.required());
      formComponent.setEnabled(annotation.enable());
      var formItem = addFormItem(formComponent, annotation.label());
      items.put(fieldName, formItem);
      binder
          .forField(formComponent)
          .bind(
              (ValueProvider<T, String>) t -> (String) util.invokeGetter(t, fieldName),
              (com.vaadin.flow.data.binder.Setter<T, String>)
                  (t, s) -> {
                    util.invokeSetter(t, fieldName, s);
                    onChange.trigger(fieldName, s, entity, items, this);
                  });
    }
  }

  private Button createSaveButton(T entity) {
    Button save = new Button("Save");
    save.setWidthFull();
    save.addClassNames("control-button", "blue-button");
    save.addClickListener(
        event -> {
          try {
            binder.writeBean(entity);
            onSave.execute(entity);
          } catch (ValidationException e) {
            log.error(e.getMessage());
          }
        });

    return save;
  }

  private Button createCancelButton(T entity) {
    Button cancel = new Button("Cancel");
    cancel.setWidthFull();
    cancel.addClassNames("control-button", "blue-button");
    cancel.addClickListener(buttonClickEvent -> onCancel.execute(entity));
    return cancel;
  }

  private Button createDeleteButton(T entity) {
    Button delete = UIUtil.createErrorPrimaryButton(this.deleteBtnName);
    delete.addClassNames("control-button", "red-button");
    delete.addClickListener(
        event -> {
          Dialog dialog = new Dialog();
          dialog.setWidth("400px");
          dialog.setCloseOnOutsideClick(false);

          Span message = new Span();
          message.setText(
              "Are you sure you want to " + deleteBtnName.toLowerCase() + " this record ?");
          message.setSizeFull();

          Button confirmButton = UIUtil.createErrorPrimaryButton("Confirm");
          confirmButton.addClassNames("control-button", "red-button");
          confirmButton.addClickListener(
              e -> {
                onDelete.execute(entity);
                dialog.close();
              });
          Button cancelButton = new Button("Cancel", e -> dialog.close());
          cancelButton.addClassNames("control-button", "blue-button");
          HorizontalLayout flexLayout = new HorizontalLayout(confirmButton, cancelButton);
          flexLayout.setWidthFull();
          flexLayout.setMargin(true);
          flexLayout.setPadding(true);
          dialog.add(message, flexLayout);
          dialog.open();
        });

    return delete;
  }

  private Component setupButtons(T entity) {
    if (entity == null) {
      try {
        Constructor<T> cons = beanType.getDeclaredConstructor();
        entity = cons.newInstance();
      } catch (NoSuchMethodException
          | IllegalAccessException
          | InstantiationException
          | InvocationTargetException e) {
        log.error(e.getMessage());
        e.printStackTrace();
      }
    }

    Button save = createSaveButton(entity);
    Button cancel = createCancelButton(entity);
    HorizontalLayout actionButtons = new HorizontalLayout(save, cancel);
    actionButtons.setWidth("50%");

    HorizontalLayout buttons = new HorizontalLayout(actionButtons);
    if (entity != null) {
      Button delete = createDeleteButton(entity);
      FlexLayout layout = new FlexLayout(delete);
      layout.setWidthFull();
      layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
      buttons.add(layout);
    }

    buttons.setHeight("200");
    buttons.setMargin(true);
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    UIUtil.setColSpan(2, buttons);
    return buttons;
  }

  private void setupUploadField(
      T entity,
      Map<String, FormItem> items,
      FormElement annotation,
      String fieldName,
      SystemUtil util) {
    VerticalLayout wrapper = new VerticalLayout();
    wrapper.setWidthFull();
    FileBuffer buffer = new FileBuffer();
    Upload upload = new Upload(buffer);
    upload.setAcceptedFileTypes("image/jpeg", "image/png", "application/pdf");
    upload.setMaxFileSize(10485760); // 10MB
    Div output = new Div();
    upload.addSucceededListener(
        event -> {
          Component component =
              createComponent(event.getMIMEType(), event.getFileName(), buffer.getInputStream());
          output.removeAll();
          showOutput(event.getFileName(), component, output);
          util.invokeSetter(entity, fieldName, event.getFileName());
        });
    upload.addAllFinishedListener(
        e -> {
          // actions when upload finished
          Notification.show("Upload Successfully").setPosition(Notification.Position.TOP_END);
        });
    upload.addFileRejectedListener(
        event -> {
          // actions when upload failed
          output.removeAll();
          Notification.show(event.getErrorMessage()).setPosition(Notification.Position.TOP_END);
        });
    wrapper.add(upload, output);
    wrapper.setPadding(true);
    FormItem uploadItem = addFormItem(wrapper, annotation.label());
    items.put(fieldName, uploadItem);
  }

  private Component createComponent(String mimeType, String fileName, InputStream stream) {
    if (mimeType.startsWith("text")) {
      return createTextComponent(stream);
    } else if (mimeType.startsWith("image")) {
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
      return image;
    }
    Div content = new Div();
    String text =
        String.format(
            "Mime type: '%s'\nSHA-256 hash: '%s'",
            mimeType, Arrays.toString(MessageDigestUtil.sha256(stream.toString())));
    content.setText(text);
    return content;
  }

  private Component createTextComponent(InputStream stream) {
    String text;
    try {
      text = IOUtils.toString(stream, StandardCharsets.UTF_8);
    } catch (IOException e) {
      text = "exception reading stream";
    }
    return new Text(text);
  }

  private void showOutput(String value, Component content, HasComponents outputContainer) {
    HtmlComponent p = new HtmlComponent(Tag.P);
    p.getElement().setText(value);
    outputContainer.add(p);
    outputContainer.add(content);
  }
}
