package io.awesome.ui.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.awesome.dto.PagingDto;
import io.awesome.ui.components.collapse.Collapse;
import io.awesome.ui.components.pagination.Pagination;
import io.awesome.ui.components.pagination.PaginationResource;
import io.awesome.ui.layout.size.Bottom;
import io.awesome.ui.layout.size.Top;
import io.awesome.ui.models.DataTableSource;
import io.awesome.ui.util.css.BoxSizing;
import io.awesome.ui.views.SelectCallback;
import io.awesome.util.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@CssImport("styles/components/data-table.css")
public class DataTable<T> extends FlexBoxLayout {
  public static final int FIRST_PAGE_INDEX = 1;
  public static final int PAGE_LIMIT = 10;
  private final DataTableSource<T> source;
  private final List<Action<Set<T>>> actions;
  private Grid<T> grid;
  private Pagination pagination;
  private MenuBar menuActionBar;
  private Button addButton;

  public DataTable(
      Class<T> entityClazz,
      String title,
      final Function<Pageable, PagingDto<T>> recordsPerPageFnc) {
    this(entityClazz, title, recordsPerPageFnc, new ArrayList<>());
  }

  public DataTable(
      Class<T> entityClazz,
      String title,
      final Function<Pageable, PagingDto<T>> recordsPerPageFnc,
      List<Action<Set<T>>> actions) {
    init();
    this.source = new DataTableSource<>(entityClazz, recordsPerPageFnc);
    this.actions = actions;
    Component body = body(entityClazz);
    Component headerControl = headerControl();
    add(
        Collapse.newBuilder()
            .setTitle(title)
            .setHeaderControl(headerControl)
            .setComponents(body)
            .build());
  }

  protected void init() {
    addClassName("data-table");
    setBoxSizing(BoxSizing.BORDER_BOX);
    setPadding(Top.S, Bottom.M);
    setFlexDirection(FlexLayout.FlexDirection.COLUMN);
  }

  protected Component body(Class<T> entityClazz) {
    VerticalLayout body = new VerticalLayout();
    body.setPadding(false);
    body.addClassName("data-table-body");
    this.grid = createGrid(entityClazz);
    this.pagination = createPagination();
    body.add(grid, pagination);
    return body;
  }

  protected Component headerControl() {
    HorizontalLayout layout = new HorizontalLayout();

    this.addButton = createAddButton();
    if (hasActions()) {
      this.menuActionBar = createActionButton();
      layout.add(createReloadButton(), menuActionBar, addButton);
    } else {
      layout.add(createReloadButton(), addButton);
    }

    return layout;
  }

  private Button createReloadButton() {
    Button reload = new Button();
    reload.setIcon(new Icon(VaadinIcon.REFRESH));
    reload.addClickListener(event -> this.reload());
    reload.getStyle().set("color", "white");
    reload.getStyle().set("border", "1px solid white");
    reload.getStyle().set("margin", "4px 0px 4px 5px");
    return reload;
  }

  private Button createAddButton() {
    Button add = new Button();
    add.setText("Create item");
    add.addClickListener(event -> {});
    add.setEnabled(false);
    add.getStyle().set("color", "white");
    add.getStyle().set("border", "1px solid white");
    add.getStyle().set("margin", "4px 0px 4px 5px");
    return add;
  }

  private MenuBar createActionButton() {
    MenuBar menuBar = new MenuBar();
    menuBar.addClassName("data-table-action-button");
    menuBar.getStyle().set("margin", "2px 0px 2px 5px");
    MenuItem item = menuBar.addItem("Action");
    item.add(new Icon(VaadinIcon.ANGLE_DOWN));

    item.getElement().getStyle().set("color", "white");
    item.getElement().getStyle().set("border", "1px solid white");
    item.getElement().getStyle().set("border-radius", "var(--lumo-border-radius-m)");
    item.getElement().getStyle().set("margin", "0");
    item.getElement().getStyle().set("cursor", "pointer");
    addAction(item);
    return menuBar;
  }

  private void addAction(MenuItem item) {
    SubMenu subMenu = item.getSubMenu();
    subMenu.removeAll();
    if (hasActions()) {
      for (Action<Set<T>> action : actions) {
        ComponentEventListener<ClickEvent<MenuItem>> listener =
            e -> action.trigger.accept(this.grid.getSelectedItems());
        subMenu
            .addItem(action.label, listener)
            .setEnabled(action.enable.test(this.grid.getSelectedItems()));
      }
    } else {
      subMenu.addItem("No available").setEnabled(false);
    }
  }

  private void reloadActionButton() {
    this.menuActionBar.getItems().forEach(this::addAction);
  }

  protected Grid<T> createGrid(Class<T> entity) {
    GridComponent<T> grid = new GridComponent<>(entity);
    if (hasActions()) {
      grid.setSelectionMode(Grid.SelectionMode.MULTI);
    }
    grid.addSelectListener(e -> {});
    grid.setAllRowsVisible(true);
    grid.setSizeFull();
    grid.setMultiSort(false);
    grid.getColumns().forEach(column -> column.setAutoWidth(true));
    grid.recalculateColumnWidths();
    return grid;
  }

  private boolean hasActions() {
    return this.actions != null && this.actions.size() > 0;
  }

  protected Pagination createPagination() {
    Pagination pagination = Utils.createPagination(FIRST_PAGE_INDEX, PAGE_LIMIT, true);
    pagination.addPageChangeListener(this::changePage);
    return pagination;
  }

  protected void changePage(PaginationResource paginationResource) {
    PagingDto<T> records =
        this.source.load(
            PageRequest.of(paginationResource.getPage(), paginationResource.getLimit()));
    this.updatePagination(records);
    this.grid.setItems(records.getResults());
    this.grid.recalculateColumnWidths();
  }

  public void reload() {
    this.resetPagination();
    PagingDto<T> records =
        this.source.load(PageRequest.of(pagination.getPage(), pagination.getLimit()));
    Optional.ofNullable(records.getResults()).ifPresent(this.grid::setItems);
    this.grid.recalculateColumnWidths();
  }

  public List<T> getItems() {
    return this.source.getRecords().getResults();
  }

  public void setItems(List<T> items) {
    this.source.getRecords().setResults(items);
    Optional.ofNullable(this.source.getRecords().getResults()).ifPresent(this.grid::setItems);
  }

  public void addSelectMultiCallback(SelectCallback<Set<T>> selectMultiCallback) {
    if (hasActions()) {
      this.grid.addSelectionListener(
          e -> {
            selectMultiCallback.trigger(e.getAllSelectedItems());
            reloadActionButton();
          });
    }
  }

  public void addSelectCallback(SelectCallback<T> selectCallback) {
    if (!hasActions()) {
      this.grid.addSelectionListener(
          e -> e.getFirstSelectedItem().ifPresent(selectCallback::trigger));
    }
  }

  public void setAbleToCreate(boolean ableToCreate) {
    this.addButton.setEnabled(ableToCreate);
    this.addButton.setVisible(ableToCreate);
  }

  public void addCreateCallback(Consumer<ClickEvent<?>> createCallBack) {
    this.addButton.addClickListener(createCallBack::accept);
  }

  public void select(T item) {
    this.grid.select(item);
  }

  public Pagination getPagination() {
    return this.pagination;
  }

  public void setPageLimit(int pageLimit) {
    this.pagination.getResource().setPage(pageLimit);
  }

  public void hidePagination(boolean hide) {
    this.pagination.setVisible(!hide);
  }

  private void resetPagination() {
    this.pagination.getResource().setPage(FIRST_PAGE_INDEX);
    this.pagination.getResource().setLimit(PAGE_LIMIT);
    this.pagination.getResource().setHasNext(true);
  }

  private void updatePagination(PagingDto<T> pagingDto) {
    this.pagination.getResource().setPage(pagingDto.getPageable().getPageNumber());
    this.pagination.getResource().setLimit(pagingDto.getPageable().getPageSize());
    this.pagination.getResource().setHasNext(pagingDto.isHasNext());
    this.pagination.getResource().setTotal(Optional.ofNullable(pagingDto.getTotal()).orElse(0L));
    this.pagination.updatePaginationButtonsStatus();
  }

  @Getter
  @Setter
  @AllArgsConstructor
  public static class Action<E> {
    private String label;
    private Consumer<E> trigger;
    private Predicate<E> enable;
  }
}
