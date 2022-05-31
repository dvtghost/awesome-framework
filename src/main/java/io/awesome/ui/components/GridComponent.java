package io.awesome.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.awesome.enums.IAttributeEnum;
import io.awesome.ui.annotations.GridColumn;
import io.awesome.ui.components.common.EnumBadge;
import io.awesome.ui.views.Callback;
import io.awesome.ui.views.SelectCallback;
import io.awesome.util.SystemUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@CssImport(value = "./styles/components/vaadin-grid-styles.css", themeFor = "vaadin-grid")
public class GridComponent<L> extends Grid<L> implements IEventListener, ISelectListener {
  @Getter @Setter private Class<L> beanType;
  @Getter @Setter private Collection<L> dataSet = new ArrayList<>();
  @Getter @Setter private ListDataProvider<L> dataProvider;

  private Callback callback;
  private SelectCallback<L> selectCallback;

  public GridComponent(Class<L> beanType) {
    this.beanType = beanType;
    createGrid();
  }

  public void setData(Collection<L> data) {
    setItems(data);
  }

  private void createGrid() {
    dataProvider = DataProvider.ofCollection(getDataSet());
    addClassName("grid-component");
    setSizeFull();
    setSelectionMode(SelectionMode.SINGLE);
    setItems(dataProvider);
    addSelectionListener(
        e -> e.getFirstSelectedItem().ifPresent(t -> this.selectCallback.trigger(t)));

    for (Field field : beanType.getDeclaredFields()) {
      if (field.isAnnotationPresent(GridColumn.class)) {
        GridColumn annotation = field.getAnnotation(GridColumn.class);
        ComponentRenderer<Component, L> componentRenderer =
            new ComponentRenderer<>(
                t -> {
                  Object value = SystemUtil.getInstance().invokeGetter(t, field.getName());
                  if (value == null) {
                    return new Span();
                  }
                  if (value instanceof IAttributeEnum) {
                    //return new Span(((IAttributeEnum<?, ?>) value).getLabel());
                      return new EnumBadge((IAttributeEnum<?, ?>) value);
                  }
                  if (value instanceof Boolean) {
                    Div wrapper = new Div();
                    wrapper.setWidthFull();
                    Icon icon;
                    if (value.equals(Boolean.FALSE)) {
                      icon = new Icon(VaadinIcon.CLOSE_CIRCLE);
                      icon.setColor("red");
                    } else {
                      icon = new Icon(VaadinIcon.CHEVRON_CIRCLE_DOWN);
                      icon.setColor("green");
                    }
                    icon.setSize("15px");
                    wrapper.add(icon);
                    return wrapper;
                  }
                  if (value instanceof LocalDate) {
                    String dateFormat =
                        ((LocalDate) value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    return new Span(dateFormat);
                  }
                  if (value instanceof LocalTime) {
                    String timeFormat =
                        ((LocalTime) value).format(DateTimeFormatter.ofPattern("HH:mm"));
                    return new Span(timeFormat);
                  }
                  if (value instanceof Component) {
                    return (Component) value;
                  }
                  return new Span(String.valueOf(value));
                });

        addColumn(componentRenderer)
            .setKey(field.getName())
            .setAutoWidth(annotation.autoWidth())
            .setFlexGrow(annotation.flexGrow())
            .setFrozen(annotation.frozen())
            .setHeader(annotation.header())
            .setSortable(annotation.sortable())
            .setTextAlign(annotation.textAlign())
            .setClassNameGenerator(attachment -> "grid-item");
      }
    }

    addThemeVariants(
        GridVariant.LUMO_NO_BORDER,
        GridVariant.LUMO_ROW_STRIPES,
        GridVariant.LUMO_WRAP_CELL_CONTENT);
  }

  @Override
  public void addEventListener(Callback callback) {
    this.callback = callback;
  }

  @Override
  public void addSelectListener(SelectCallback callback) {
    this.selectCallback = callback;
  }
}
