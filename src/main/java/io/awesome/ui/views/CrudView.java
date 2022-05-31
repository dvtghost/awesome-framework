package io.awesome.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.awesome.ui.components.button.CancelButton;
import io.awesome.ui.components.button.DeleteButton;
import io.awesome.ui.components.button.SaveButton;
import io.awesome.dto.FilterDto;
import io.awesome.dto.PageSessionDto;
import io.awesome.dto.PagingDto;
import io.awesome.exception.BaseException;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.components.*;
import io.awesome.ui.components.collapse.Collapse;
import io.awesome.ui.components.detailsdrawer.DetailsDrawer;
import io.awesome.ui.components.detailsdrawer.DetailsDrawerHeader;
import io.awesome.ui.components.pagination.Pagination;
import io.awesome.ui.layout.size.Bottom;
import io.awesome.ui.layout.size.Horizontal;
import io.awesome.ui.layout.size.Top;
import io.awesome.ui.models.Searchable;
import io.awesome.ui.util.css.BoxSizing;
import io.awesome.util.Utils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;

import static io.awesome.constant.SessionActions.EDIT_ENTITY;
import static io.awesome.constant.SessionActions.NEW_ENTITY;

@CssImport("styles/components/crud-view.css")
public abstract class CrudView<F extends BaseFilterUI, L, E, M extends CrudMapper<L, E>>
    extends SplitViewFrame {
  public static final int FIRST_PAGE_INDEX = 1;
  public static final int PAGE_LIMIT = 10;
  protected final M mapper;
  @Getter protected final Pagination pagination;
  private final Logger logger = LoggerFactory.getLogger(CrudView.class);
  private final Class<F> filterClazz;
  private final Class<E> editEntityClazz;
  @Getter @Setter protected GridComponent<L> grid;
  @Getter @Setter protected String tableTitle = "";
  @Getter @Setter protected String newButtonTitle;
  @Getter @Setter protected String deleteBtnName = "Delete";
  protected Component headerComponent;
  protected ExtendedBinder<F> filterFormBinder;
  protected DetailsDrawer detailsDrawer;
  protected HorizontalLayout filterWrapper;
  @Getter private String detailTitle;
  private F filterEntity;
  @Getter @Setter private FormLayout createOrUpdateForm;
  @Getter @Setter private Map<String, L> items = new HashMap<>();

  public CrudView(Class<F> filterClazz, Class<L> listEntity, Class<E> editEntityClazz, M mapper) {
    this.mapper = mapper;
    this.filterClazz = filterClazz;
    this.editEntityClazz = editEntityClazz;
    this.grid = createGrid(listEntity);
    this.pagination = Utils.createPagination(FIRST_PAGE_INDEX, PAGE_LIMIT, true);
  }

  public abstract String getRoute();

  public PagingDto<L> getPagingData() {
    List<FilterDto> filters = prepareFilters(getFilterEntity());
    PagingDto<L> pagingDto = getPagingDataWithFilters(filters);
    updatePagination(pagingDto);
    return pagingDto;
  }

  private void resetPagination() {
    this.pagination.getResource().setPage(FIRST_PAGE_INDEX);
    this.pagination.getResource().setLimit(PAGE_LIMIT);
    this.pagination.getResource().setHasNext(true);
  }

  protected void updatePagination(PagingDto<L> pagingDto) {
    this.pagination.getResource().setPage(pagingDto.getPageable().getPageNumber());
    this.pagination.getResource().setLimit(pagingDto.getPageable().getPageSize());
    this.pagination.getResource().setHasNext(pagingDto.isHasNext());
    this.pagination.updatePaginationButtonsStatus();
  }

  public abstract PagingDto<L> getPagingDataWithFilters(List<FilterDto> filters);

  protected GridComponent<L> createGrid(Class<L> listEntity) {
    GridComponent<L> grid = new GridComponent<>(listEntity);
    grid.setAllRowsVisible(true);
    grid.setSizeFull();
    grid.setMultiSort(false);
    grid.getColumns().forEach(column -> column.setAutoWidth(true));
    grid.recalculateColumnWidths();
    grid.addSelectListener(
        new SelectCallback<L>() {
          @Override
          public void trigger(L entity) {
            E e = mapper.fromListToEdit(entity);
            onPreEditPageRendering(e);
            showDetails(e);
          }
        });
    return grid;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    if (this.filterEntity == null) {
      this.filterEntity = buildNewFilterUIEntity();
    }
    this.headerComponent = createHeadComponent();
    onInit();
    setViewContent(headerComponent, createToolbar(), createContent());
    setViewDetails(createDetailsDrawer());
    filter();
    pagination.addPageChangeListener(event -> searchData(getFilterEntity()));
    handleSessionActions();
  }

  private F getFilterEntity() {
    if (this.filterEntity == null) {
      this.filterEntity = buildNewFilterUIEntity();
    }
    return this.filterEntity;
  }

  private void handleSessionActions() {
    try {
      PageSessionDto sessionDto =
          (PageSessionDto) UI.getCurrent().getSession().getAttribute(getRoute());
      if (sessionDto != null) {
        switch (sessionDto.getAction()) {
          case NEW_ENTITY:
            showNewEntityForm();
            break;
          case EDIT_ENTITY:
            showEditEntity(sessionDto);
            break;
        }
        clearSession();
      }
    } catch (Exception e) {
      throw new BaseException(
          String.format("Can't handle session actions %s", e.getLocalizedMessage()), e);
    }
  }

  protected void showEditEntity(PageSessionDto sessionDto) {
    this.grid.select(this.getItems().get((String) sessionDto.getData().get("id")));
  }

  private void clearSession() {
    UI.getCurrent().getSession().setAttribute(getRoute(), null);
  }

  public VerticalLayout createHeadComponent() {
    filterWrapper = createFilterComponents();
    VerticalLayout wrapper = new VerticalLayout(filterWrapper);
    wrapper.setPadding(false);
    return wrapper;
  }

  public HorizontalLayout createFilterComponents() {
    HorizontalLayout filterWrapper = Utils.createHeadComponent();
    FilterForm<F> form = createFilterForm(filterWrapper);
    filterWrapper.add(form);
    return filterWrapper;
  }

  private FilterForm<F> createFilterForm(HorizontalLayout filterWrapper) {
    filterFormBinder = new ExtendedBinder<>();
    filterFormBinder.setBean(filterEntity);
    FilterControl<F> filterControl =
        new FilterControl<>(
            filterEntity,
                isAbleToSaveFilter(),
            getAllSavedFilterNames(),
            (name, entity) -> {
              saveFilter(name, entity);
              return Pair.of(name, entity);
            },
            (name) -> {
              F saved = loadFilter(name);
              filterFormBinder.readBean(filterEntity);
              ModelMapper modelMapper = new ModelMapper();
              modelMapper.map(saved, filterEntity);
              filterWrapper.removeAll();
              this.headerComponent = createHeadComponent();
              setViewContent(headerComponent, createToolbar(), createContent());
              return filterEntity;
            },
            this::removeFilter,
            () -> {
              resetPagination();
              searchData(filterEntity);
              return filterEntity;
            },
            () -> {
              resetFilter();
              return filterEntity;
            });
    FilterForm<F> form =
        new FilterForm<>(
            filterClazz,
            filterEntity,
            filterFormBinder,
            getAllSavedFilterNames(),
            (fieldName, value, event, items, formLayout) -> {},
            this::filterFormInit,
            filterControl);
    form.addClassNames("no-padding-left", "no-padding-right");
    return form;
  }

  protected void filterFormInit(F entity, Map<String, FormLayout.FormItem> formItems) {}

  private F loadFilter(String name) {
    F filter = getFilter(name);
    this.filterFormBinder.removeBean();
    this.filterFormBinder.setBean(filter);
    return filter;
  }

  protected abstract F removeFilter(String name);

  protected abstract F getFilter(String name);

  protected abstract void saveFilter(String name, F entity);

  protected abstract Set<String> getAllSavedFilterNames();

  protected void searchData(F filterEntity) {
    List<FilterDto> filters = prepareFilters(filterEntity);
    PagingDto<L> pagingDto = getPagingDataWithFilters(filters);
    updatePagination(pagingDto);
    setGridData(pagingDto.getResults());
  }

  protected void resetFilter() {
    F newEntity = buildNewFilterUIEntity();
    filterFormBinder.readBean(filterEntity);
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.map(newEntity, filterEntity);
    filterFormBinder.setBean(filterEntity);
    filterWrapper.removeAll();
    this.headerComponent = createHeadComponent();
    setViewContent(headerComponent, createToolbar(), createContent());
  }

  protected abstract List<FilterDto> prepareFilters(F filterEntity);

  protected F buildNewFilterUIEntity() {
    logger.debug("Default buildFilterUIEntity");
    try {
      return ConstructorUtils.invokeConstructor(filterClazz, null);
    } catch (Exception e) {
      logger.error(String.format("Can't invoke constructor from class %s", filterClazz));
      throw new RuntimeException(
          String.format(
              "Can't invoke constructor from class %s - %s", filterClazz, e.getLocalizedMessage()));
    }
  }

  protected DetailsDrawer createDetailsDrawer() {
    detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.RIGHT);
    detailsDrawer.setHeader(createDetailsDrawerHeader());
    return detailsDrawer;
  }

  protected DetailsDrawerHeader createDetailsDrawerHeader() {
    DetailsDrawerHeader detailsDrawerHeader = new DetailsDrawerHeader(getDetailTitle(), new Div());
    detailsDrawerHeader.addCloseListener(buttonClickEvent -> detailsDrawer.hide());
    return detailsDrawerHeader;
  }

  protected Component createToolbar() {
    Toolbar<E> toolbar = new Toolbar<>(newButtonTitle, isAbleToCreate());

    toolbar.addEventListener(
        new Callback() {
          @Override
          public void trigger(ComponentEvent<?> event) {
            if (event.getSource() instanceof Button) {
              try {
                showNewEntityForm();
              } catch (Exception e) {
                throw new BaseException(
                    String.format(
                        "Error when try show new entity form %s", e.getLocalizedMessage()),
                    e);
              }
            }
          }
        });

    return toolbar;
  }

  protected void showNewEntityForm() throws Exception {
    Constructor<E> entity = editEntityClazz.getDeclaredConstructor();
    E obj = entity.newInstance();
    onPreNewPageRendering(obj);
    showDetails(obj);
  }

  protected Component createContent() {
    FlexBoxLayout content = new FlexBoxLayout();
    content.addClassName("component-content");
    content.setBoxSizing(BoxSizing.BORDER_BOX);
    content.setPadding(Horizontal.RESPONSIVE_X, Top.S, Bottom.M);
    content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);

    VerticalLayout body = new VerticalLayout();
    body.setPadding(false);
    body.addClassName("component-body");
    body.add(grid, pagination);
    content.add(Collapse.newBuilder().setTitle(getTableTitle()).setComponents(body).build());
    return content;
  }

  protected void showDetails(E entity) {
    detailsDrawer.setContent(createDetails(entity));
    detailsDrawer.show();
  }

  public void setDetailTitle(String detailTitle) {
    this.detailTitle = detailTitle;
    if (!Objects.isNull(detailsDrawer)) detailsDrawer.setHeader(createDetailsDrawerHeader());
  }

  private Component createDetails(E entity) {
    return createEditor(entity);
  }

  private FormLayout createEditor(E edit) {
    ExtendedBinder<E> binder = new ExtendedBinder<>();
    binder.setBean(edit);
    AbstractForm<E> form =
        new AbstractForm<>(
            editEntityClazz,
            edit,
            binder,
            isAbleToEdit(),
            this::onFormValuesChange,
            this::onFormLoad,
            "");
    FormControl formControl =
        new FormControl(form)
            .addActionBtns(new SaveButton(), new CancelButton(), new DeleteButton());
    setupDetailFormActionButtons(formControl, form);
    form.setFormControl(formControl);
    detailsDrawer.setContent(form);
    detailsDrawer.show();
    return form;
  }

  private void setupDetailFormActionButtons(FormControl formControl, AbstractForm form) {
    formControl
        .getActionButton(SaveButton.TYPE_SAVE)
        .ifPresent(
            btn ->
                btn.addActionHandler(
                    entity -> {
                      E editEntity = (E) entity;
                      onSave(editEntity);
                    },
                    form,
                    logger));
    formControl
        .getActionButton(CancelButton.TYPE_CANCEL)
        .ifPresent(
            btn ->
                btn.addActionHandler(
                    entity -> {
                      onCancel();
                    },
                    form,
                    logger));
    formControl
        .getActionButton(DeleteButton.TYPE_DELETE)
        .ifPresent(
            btn ->
                btn.addActionHandler(
                    entity -> {
                      E editEntity = (E) entity;
                      onDelete(editEntity);
                    },
                    form,
                    logger));
    formControl.showActionButtons("50%", SaveButton.TYPE_SAVE, CancelButton.TYPE_CANCEL);
  }

  protected boolean isAbleToSaveFilter() {
    return true;
  }

  public abstract void onInit();

  public abstract void onSave(E entity);

  public abstract void onDelete(E entity);

  public abstract void onCancel();

  public abstract void onValidate(E entity);

  public abstract void filter();

  public abstract boolean isAbleToEdit();

  public abstract boolean isAbleToCreate();

  // Override methods
  protected void onPreEditPageRendering(E editEntity) {
    setDetailTitle(String.join(" ", "Update", this.tableTitle));
  }

  protected void onPreNewPageRendering(E editEntity) {
    setDetailTitle(String.join(" ", "Create", this.tableTitle));
  }

  // TOTO Dang remove Form class so that we can use AbstractForm here
  protected void onFormValuesChange(
      String fieldName,
      Object value,
      E editEntity,
      Map<String, FormLayout.FormItem> items,
      FormLayout form) {}

  protected void onFormLoad(E editEntity, Map<String, FormLayout.FormItem> items) {}

  protected void setGridData(Collection<L> data) {
    grid.setData(data);
    grid.getColumns().forEach(column -> column.setAutoWidth(true));
    grid.recalculateColumnWidths();
  }

  protected Form<Searchable> onInitSearchForm() {
    return null;
  }
}
