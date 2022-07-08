package io.awesome.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
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
import io.awesome.dto.FilterDto;
import io.awesome.dto.PageSessionDto;
import io.awesome.dto.PagingDto;
import io.awesome.exception.BaseException;
import io.awesome.exception.SystemException;
import io.awesome.exception.ValidateException;
import io.awesome.ui.binder.ExtendedBinder;
import io.awesome.ui.components.*;
import io.awesome.ui.components.button.CancelButton;
import io.awesome.ui.components.button.DeleteButton;
import io.awesome.ui.components.button.SaveButton;
import io.awesome.ui.components.datatable.DataTable;
import io.awesome.ui.components.detailsdrawer.DetailsDrawer;
import io.awesome.ui.components.detailsdrawer.DetailsDrawerHeader;
import io.awesome.ui.layout.size.Bottom;
import io.awesome.ui.layout.size.Horizontal;
import io.awesome.ui.layout.size.Top;
import io.awesome.ui.models.Searchable;
import io.awesome.ui.util.css.BoxSizing;
import io.awesome.util.FormUtil;
import io.awesome.util.NotificationUtil;
import io.awesome.util.Utils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
  private final Logger logger = LoggerFactory.getLogger(CrudView.class);
  private final Class<F> filterClazz;
  private final Class<E> editEntityClazz;
  private final Class<L> listEntityClazz;
  private final List<FilterDto> filters;
  @Getter @Setter protected GridComponent<L> grid;
  @Getter @Setter protected String tableTitle = "";
  @Getter @Setter protected String newButtonTitle;
  @Getter @Setter protected String deleteBtnName = "Delete";
  protected Component headerComponent;
  protected List<Component> contentComponents;
  protected ExtendedBinder<F> filterFormBinder;
  protected DetailsDrawer detailsDrawer;
  protected HorizontalLayout filterWrapper;
  protected DataTable<L> dataTable;
  FilterForm<F> filterForm;
  @Getter private String detailTitle;
  private F filterEntity;
  @Getter @Setter private FormLayout createOrUpdateForm;
  @Getter @Setter private Map<String, L> items = new HashMap<>();

  public CrudView(Class<F> filterClazz, Class<L> listEntity, Class<E> editEntityClazz, M mapper) {
    this.mapper = mapper;
    this.filterClazz = filterClazz;
    this.listEntityClazz = listEntity;
    this.editEntityClazz = editEntityClazz;
    this.filters = new ArrayList<>();
  }

  public abstract String getRoute();

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
    handleSessionActions();
  }

  protected Component[] createViewContents() {
    this.headerComponent = createHeadComponent();
    this.contentComponents = createContents();
    List<Component> viewContents = new ArrayList<>(Arrays.asList(headerComponent));
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
    this.dataTable.select(this.getItems().get((String) sessionDto.getData().get("id")));
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
    this.filters.clear();
    this.filters.addAll(prepareFilters(filterEntity));
    reloadDataTable();
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

  protected void showNewEntityForm() throws Exception {
    Constructor<E> entity = editEntityClazz.getDeclaredConstructor();
    E obj = entity.newInstance();
    onPreNewPageRendering(obj);
    showDetails(obj);
  }

  protected List<Component> createContents() {
    List<DataTable.Action<Set<L>>> actions = dataTableActions();
    this.dataTable =
        new DataTable<L>(listEntityClazz, getTableTitle(), this::getPagingData, actions);
    this.dataTable.setPageLimit(PAGE_LIMIT);
    this.dataTable.addSelectCallback(this::selectCallBack);
    this.dataTable.addSelectMultiCallback(this::selectMultiCallBack);
    this.dataTable.addCreateCallback(this::createCallBack);
    this.dataTable.setAbleToCreate(isAbleToCreate());
    return new ArrayList<>(List.of(dataTable));
  }

  protected PagingDto<L> getPagingData(Pageable pageable) {
    List<FilterDto> filters = prepareFilters(filterEntity);
    PagingDto<L> pagingDto =
        new PagingDto<>(
            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
            filters,
            new ArrayList<>());
    return getRecordPerPage(pagingDto);
  }

  protected abstract PagingDto<L> getRecordPerPage(PagingDto<L> pagingDto);

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
        .button(new SaveButton())
        .action(
            e -> {
              this.onSave(e);
              this.onPostSave(e);
            })
        .button(new CancelButton())
        .action(this::onCancel)
        .button(new DeleteButton())
        .action(
            e -> {
              this.onDelete(e);
              this.onPostDelete(e);
            })
        .build()
        .create(entity, isAbleToEdit());
  }

  protected boolean isAbleToSaveFilter() {
    return true;
  }

  public abstract void onInit();

  public abstract void onSave(E entity);

  public abstract void onDelete(E entity);

  public abstract void onCancel();

  protected void onPostSave(E e) {
    NotificationUtil.success("Saved successfully");
  }

  protected void onPostDelete(E e) {
    NotificationUtil.success("Deleted Successfully");
  }

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

  protected void selectMultiCallBack(Set<L> ls) {}

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
      searchData(filterEntity);
    } catch (ValidateException e) {
      var errors = e.getErrors();
      FormUtil.addError(errors, this.filterForm);
      logger.error(Constants.VALIDATE_EXCEPTION_PREFIX, e);
    } catch (Exception e) {
      logger.error(Constants.EXCEPTION_PREFIX, e);
      throw new SystemException("Cannot search " + e.getMessage());
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

  protected void reloadDataTable() {
    this.dataTable.reload();
  }

  protected List<DataTable.Action<Set<L>>> dataTableActions() {
    return Collections.emptyList();
  }

  protected void createCallBack(ClickEvent<?> event) {
    if (event.getSource() instanceof Button) {
      try {
        showNewEntityForm();
      } catch (Exception e) {
        throw new BaseException(
            String.format("Error when try show new entity form %s", e.getLocalizedMessage()), e);
      }
    }
  }
}
