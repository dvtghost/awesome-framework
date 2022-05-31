package io.awesome.service;

import com.vaadin.flow.component.upload.receivers.FileBuffer;
import io.awesome.dao.AbstractIdentifyDao;
import io.awesome.dto.ErrorsDto;
import io.awesome.dto.FilterDto;
import io.awesome.dto.PagingDto;
import io.awesome.exception.ValidateException;
import io.awesome.model.BaseModel;
import io.awesome.model.IAttachmentable;
import io.awesome.ui.models.EditableID;
import io.awesome.ui.models.ListableID;
import io.awesome.util.DeepCopyBeanUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractCrudService<ID, M extends BaseModel<ID>, L extends ListableID<E, ID>, E extends EditableID<ID>>
    extends AbstractIdentifyService<M, ID> {

  public AbstractCrudService(Class<M> modelClass, AbstractIdentifyDao<M, ID> dao) {
    super(modelClass, dao);
  }

  public Iterable<M> findAll() {
    return dao.findAll();
  }

  public M mapEditEntityToModel(E e, M model) {
    DeepCopyBeanUtils.copyProperties(e, model);
    return model;
  }

  public Optional<M> findById(ID id) {
    return dao.findById(id);
  }

  public void delete(ID id) {
    dao.deleteById(id);
  }

  protected ErrorsDto validateForSave(E e, ErrorsDto errorsDto) {
    return errorsDto;
  }

  public M save(E e)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException,
          IllegalAccessException {
    M m = super.modelClass.getDeclaredConstructor().newInstance();
    if (e.getId() != null) {
      var query = findById(e.getId());
      m = query.orElse(super.modelClass.getDeclaredConstructor().newInstance());
    }

    ErrorsDto errors = validateForSave(e, new ErrorsDto());
    if (errors.hasErrors()) {
      unifyError(errors);
      throw new ValidateException(errors);
    }

    m = mapEditEntityToModel(e, m);
    m = preSave(m);
    // because dao save wil clear all the transient value, thus we need to take out the
    // file here
    FileBuffer attachment = null;
    if (m instanceof IAttachmentable) {
      attachment = ((IAttachmentable) m).getAttachment();
    }

    m = dao.save(m);
    m = postSave(m);

    if (m instanceof IAttachmentable) {
      m = processingAttachment(m, attachment);
    }

    return m;
  }

  @NotNull
  private List<L> modelMapping(Class<L> clazz, Iterable<M> entities)
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
          NoSuchMethodException {
    List<L> result = new ArrayList<>();

    for (M m : entities) {
      var l = clazz.getDeclaredConstructor().newInstance();
      DeepCopyBeanUtils.copyProperties(m, l);
      result.add(l);
    }
    return result;
  }

  public PagingDto<L> searchByFilters(Class<L> clazz, PagingDto<L> pagingDto, boolean fullInit)
      throws InvocationTargetException, InstantiationException, IllegalAccessException,
          NoSuchMethodException {
    PagingDto<M> baseModelPagingDto =
        new PagingDto<>(pagingDto.getPageable(), pagingDto.getFilters(), new ArrayList<>());
    baseModelPagingDto = dao.searchByFilters(this.modelClass, baseModelPagingDto, fullInit);

    return new PagingDto<>(
        baseModelPagingDto.getPageable(),
        baseModelPagingDto.getFilters(),
        modelMapping(clazz, baseModelPagingDto.getResults()));
  }

  public PagingDto<L> getRecordPerPageUIList(PagingDto<L> pagingDto) {
    PagingDto<M> recordPagingDto =
            getRecordPerPage(pagingDto.getPageable(), pagingDto.getFilters());
    Optional<List<M>> records = Optional.ofNullable(recordPagingDto.getResults());

    records.map(items -> items.stream().map(this::mapEntityToUIList).collect(Collectors.toList()))
            .ifPresent(pagingDto::setResults);
    pagingDto.setHasNext(recordPagingDto.isHasNext());
    pagingDto.setPageable(recordPagingDto.getPageable());
    return pagingDto;
  }

  public PagingDto<M> getRecordPerPage(Pageable pageable, List<FilterDto> filters) {
    return dao.searchByFilters(
            super.modelClass, new PagingDto<>(pageable, filters, new ArrayList<>()), true);
  }

  public L mapEntityToUIList(M model)  {
    var listItem = getListableSupplier().get();
    E editEntity = getEditableSupplier().get();
    List<String> ignoreProperties = getIgnoreProperties();
    DeepCopyBeanUtils.copyProperties(model, listItem, ignoreProperties.toArray(new String[0]));
    postCopyEntityToUIList(model, listItem);
    DeepCopyBeanUtils.copyProperties(model, editEntity, ignoreProperties.toArray(new String[0]));
    postCopyEntityToUIEdit(model, editEntity);
    listItem.setEntity(editEntity);
    return listItem;
  }

  protected List<String> getIgnoreProperties() {
    return Collections.emptyList();
  }

  /**
   * Customizing the UI list after copying properties from entity
   * @param model
   * @param listItem
   */
  protected void postCopyEntityToUIList(M model, L listItem) {
  }

  /**
   * Customizing the UI edit after copying properties from entity
   * @param model
   * @param editItem
   */
  protected void postCopyEntityToUIEdit(M model, E editItem) {
  }

  /**
   * Supplier for initializing UI list
   * @return {@link io.awesome.ui.models.ListableID}
   */
  public abstract Supplier<L> getListableSupplier();

  /**
   * Supplier for initializing UI edit
   * @return {@link io.awesome.ui.models.EditableID}
   */
  public abstract Supplier<E> getEditableSupplier();
}
