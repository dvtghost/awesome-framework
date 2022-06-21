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
import com.vaadin.flow.data.provider.DataProvider;
import io.awesome.config.Constants;
import io.awesome.exception.ValidateException;
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
import io.awesome.ui.views.Callback;
import io.awesome.ui.views.CrudMapper;
import io.awesome.ui.views.SelectCallback;
import io.awesome.ui.views.SplitViewFrame;
import io.awesome.util.FormUtil;
import io.awesome.util.Utils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
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
  protected List<Component> contentComponents;
  protected ExtendedBinder<F> filterFormBinder;
  protected DetailsDrawer detailsDrawer;
  protected HorizontalLayout filterWrapper;
  @Getter private String detailTitle;
  private F filterEntity;
  @Getter @Setter private FormLayout createOrUpdateForm;
  @Getter @Setter private Map<String, L> items = new HashMap<>();
  FilterForm<F> filterForm;

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
    this.pagination.getResource().setTotal(Optional.ofNullable(pagingDto.getTotal()).orElse(0L));
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
    grid.addSelectListener(addSelectCallback());
    return grid;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    if (this.filterEntity == null) {
      this.filterEntity = buildNewFilterUIEntity();
    }
    setViewContent(createViewContents());
    setViewDetails(createDetailsDrawer());
    onInit();
    filter();
    pagination.addPageChangeListener(event -> searchData(getFilterEntity()));
    handleSessionActions();
  }

  @NotNull
  protected Component[] createViewContents() {
    this.headerComponent = createHeadComponent();
    this.contentComponents = createContents();
    List<Component> viewContents = new ArrayList<>(Arrays.asList(headerComponent, createToolbar()));
    viewContents.addAll(this.contentComponents);
    return viewContents.toArray(new Component[0]);
  }

  protected F getFilterEntity() {
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
                    this::onSaveFilter,
                    this::onLoadFilter,
                    this::onRemoveFilter,
                    this::onSearch,
                    this::onResetFilter);
    filterForm =
            new FilterForm<>(
                    filterClazz,
                    filterEntity,
                    filterFormBinder,
                    getAllSavedFilterNames(),
                    (fieldName, value, event, items, formLayout) -> {},
                    this::filterFormInit,
                    filterControl);
    filterForm.addClassNames("no-padding-left", "no-padding-right");
    return filterForm;
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
    setViewContent(createViewContents());
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

  protected List<Component> createContents() {
    FlexBoxLayout content = createFlexBoxLayoutContent();
    VerticalLayout body = new VerticalLayout();
    body.setPadding(false);
    body.addClassName("component-body");
    body.add(grid, pagination);
    content.add(Collapse.newBuilder().setTitle(getTableTitle()).setComponents(body).build());
    return new ArrayList<>(List.of(content));
  }

  protected FlexBoxLayout createFlexBoxLayoutContent() {
    FlexBoxLayout content = new FlexBoxLayout();
    content.addClassName("component-content");
    content.setBoxSizing(BoxSizing.BORDER_BOX);
    content.setPadding(Horizontal.RESPONSIVE_X, Top.S, Bottom.M);
    content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);

    return content;
  }

  protected void showDetails(E entity) {
    showDetails(createDetails(entity));
  }

  protected void showDetails(Component detailContent) {
    detailsDrawer.setContent(detailContent);
    detailsDrawer.show();
  }

  public void setDetailTitle(String detailTitle) {
    this.detailTitle = detailTitle;
    if (!Objects.isNull(detailsDrawer)) detailsDrawer.setHeader(createDetailsDrawerHeader());
  }

  private Component createDetails(E entity) {
    return new EditorView.EditorViewBuilder<>(editEntityClazz)
            .onChange(this::onFormValuesChange)
            .onInit(this::onFormLoad)
            .button(new SaveButton()).action(this::onSave)
            .button(new CancelButton()).action(this::onCancel)
            .button(new DeleteButton()).action(this::onDelete)
            .build().create(entity, isAbleToEdit());
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
    setGridData(grid, data);
  }

  protected <T> void setGridData(GridComponent<T> grid, Collection<T> data) {
    grid.setDataProvider(DataProvider.ofCollection(data));
    grid.setData(data);
    grid.getColumns().forEach(column -> column.setAutoWidth(true));
    grid.recalculateColumnWidths();
    grid.setVisible(true);
  }

  protected Form<Searchable> onInitSearchForm() {
    return null;
  }

  protected void selectCallBack(L entity) {
    E e = mapper.fromListToEdit(entity);
    onPreEditPageRendering(e);
    showDetails(e);
  }

  @NotNull
  protected SelectCallback<L> addSelectCallback() {
    return new SelectCallback<L>() {
      @Override
      public void trigger(L entity) {
        E e = mapper.fromListToEdit(entity);
        onPreEditPageRendering(e);
        showDetails(e);
      }
    };
  }

  protected Pair<String, F> onSaveFilter(String filterName, F filter) {
    saveFilter(filterName, filter);
    return Pair.of(filterName, filter);
  }

  protected F onLoadFilter(String filterName) {
    F saved = loadFilter(filterName);
    filterFormBinder.readBean(filterEntity);
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.map(saved, filterEntity);
    filterWrapper.removeAll();
    setViewContent(createViewContents());
    return filterEntity;
  }

  protected F onSearch() {
    try {
      this.filterFormBinder.writeBean(this.filterEntity);
      resetPagination();
      searchData(filterEntity);
    } catch (ValidateException e) {
      var errors = e.getErrors();
      FormUtil.addError(errors, this.filterForm);
      logger.error(Constants.VALIDATE_EXCEPTION_PREFIX, e);
    } catch (Exception e) {
      logger.error(Constants.EXCEPTION_PREFIX, e);
    }
    return filterEntity;
  }

  protected F onRemoveFilter(String filterName) {
    return removeFilter(filterName);
  }

  protected F onResetFilter() {
    resetFilter();
    return filterEntity;
  }
}
