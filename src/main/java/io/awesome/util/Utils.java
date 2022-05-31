package io.awesome.util;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.awesome.ui.components.pagination.Pagination;
import io.awesome.ui.components.pagination.PaginationResource;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.ArrayList;
import java.util.Arrays;

public class Utils<T> {

  public static HorizontalLayout createHeadComponent() {
    HorizontalLayout headLayout = new HorizontalLayout();
    headLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    headLayout.addClassNames("head-layout");
    return headLayout;
  }

  public static Pagination createPagination(int page, int limit, boolean hasNext) {
    final PaginationResource paginationResource =
        PaginationResource.newBuilder().setPage(page).setLimit(limit).setHasNext(hasNext).build();
    return new Pagination(paginationResource);
  }

  public static MultiselectComboBox<String> createMultiSelectComboBox(
      String label, String... items) {
    MultiselectComboBox<String> comboBox = new MultiselectComboBox<>();
    comboBox.setLabel(label);
    comboBox.setItems(items);
    comboBox.addClassNames("head-input");
    comboBox.setClearButtonVisible(true);
    return comboBox;
  }

  public static ComboBox<String> createComboBox(String label, String... items) {
    ComboBox<String> comboBox = new ComboBox<>();
    comboBox.setLabel(label);
    comboBox.setItems(items);
    comboBox.addClassNames("head-input");
    comboBox.setClearButtonVisible(true);
    return comboBox;
  }

  public static MultiselectComboBox<String> createMultiComboBox(String label, String... items) {
    MultiselectComboBox<String> comboBox = new MultiselectComboBox<>();
    comboBox.setLabel(label);
    comboBox.setItems(items);
    comboBox.addClassNames("head-input");
    comboBox.setClearButtonVisible(true);
    return comboBox;
  }

  public static TextField createTextField(String label) {
    TextField textField = new TextField();
    textField.addClassName("head-input");
    textField.setLabel(label);
    textField.setClearButtonVisible(true);
    return textField;
  }

  public static Row createDateRangePicker(String startDateLabel, String endDateLabel) {
    DatePicker startDate = new DatePicker();
    startDate.setLabel(startDateLabel);
    DatePicker endDate = new DatePicker();
    endDate.setLabel(endDateLabel);
    startDate.addValueChangeListener(e -> endDate.setMin(e.getValue()));
    startDate.addClassNames("head-input", "date-picker-custom-left");
    endDate.addClassNames("head-input", "date-picker-custom-right");
    Row dateRow = new Row(startDate, endDate);
    dateRow.addClassNames("wrapper-row");
    return dateRow;
  }

  public static Div createHeaderComponent(String title) {
    Div headerBody = new Div();
    headerBody.addClassName("header-table");
    Span headerTitle = new Span(title);
    headerTitle.addClassName("header-title");
    headerBody.add(headerTitle);
    return headerBody;
  }

  public static Div createHeaderComponent(String title, String... classnames) {
    Div headerBody = new Div();
    headerBody.addClassName("header-table");
    headerBody.addClassNames(classnames);
    Span headerTitle = new Span(title);
    headerTitle.addClassName("header-title");
    headerBody.add(headerTitle);
    return headerBody;
  }

  public static void notification(String message) {
    Notification.show(message).setPosition(Notification.Position.TOP_END);
  }

  public static Button createTextButton(Span span, Dialog dialog) {
    Button textButton = new Button(span);
    textButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    textButton.addClickListener(
        e -> {
          dialog.open();
        });
    return textButton;
  }

  public static Dialog createDialog(
      String text,
      Component component,
      ComponentEventListener<ClickEvent<Button>> confirmEvent,
      ComponentEventListener<ClickEvent<Button>> cancelEvent) {
    Dialog dialog = new Dialog();
    dialog.setWidth("40%");
    dialog.setCloseOnOutsideClick(false);
    dialog.add(new H4(text));

    Button confirmButton = new Button(new Icon(VaadinIcon.CHECK), confirmEvent);
    confirmButton.addClickListener(e -> dialog.close());
    confirmButton.addClassNames("control-button", "blue-button", "dialog-button");

    Button cancelButton = new Button(new Icon(VaadinIcon.CLOSE), cancelEvent);
    cancelButton.addClickListener(e -> dialog.close());
    cancelButton.addClassNames("control-button", "blue-button", "dialog-button");

    Div body = new Div(component, confirmButton, cancelButton);
    body.getStyle().set("display", "flex");
    body.getStyle().set("justify-content", "center");
    body.getStyle().set("align-items", "center");
    dialog.add(body);
    Shortcuts.addShortcutListener(dialog, dialog::close, Key.ESCAPE);
    return dialog;
  }

  public ArrayList<T> getRecordPerPage(ArrayList<T> list, int page, int limit) {
    int arrayLimit = Math.min(limit * page, list.size());
    return new ArrayList<>(list.subList((page - 1) * limit, arrayLimit));
  }

  public void setFrozenColumn(Grid<T> grid, int... columns) {
    Arrays.stream(columns)
        .forEach(
            column -> {
              grid.getColumns().get(column).setFrozen(true);
            });
    grid.getColumns().forEach(column -> column.setAutoWidth(true));
    grid.recalculateColumnWidths();
  }
}
