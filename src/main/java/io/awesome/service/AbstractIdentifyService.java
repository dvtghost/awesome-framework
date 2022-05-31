package io.awesome.service;

import com.vaadin.flow.component.upload.receivers.FileBuffer;
import io.awesome.dao.AbstractIdentifyDao;
import io.awesome.dto.ErrorDto;
import io.awesome.dto.ErrorsDto;
import io.awesome.dto.PagingDto;
import io.awesome.exception.ValidateException;
import io.awesome.model.BaseModel;
import io.awesome.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public abstract class AbstractIdentifyService<M extends BaseModel<ID>, ID> {
  protected final Class<M> modelClass;
  protected AbstractIdentifyDao<M, ID> dao;
  //  @Autowired private AttachmentHelper attachmentHelper;

  public AbstractIdentifyService(Class<M> modelClass) {
    this.modelClass = modelClass;
  }

  public AbstractIdentifyService(Class<M> modelClass, AbstractIdentifyDao<M, ID> dao) {
    this.modelClass = modelClass;
    this.dao = dao;
  }

  protected M preSave(M entity) {
    return entity;
  }

  protected M postSave(M entity) {
    return entity;
  }

  protected M processingAttachment(M obj, FileBuffer attachment) {
    return obj;
  }

  public PagingDto find(PagingDto pagingDto) {
    return dao.searchByFilters(modelClass, pagingDto, false);
  }

  protected ErrorsDto validateForSave(M model, ErrorsDto errorsDto) {
    return errorsDto;
  }

  public M save(M m) {
    ErrorsDto errors = validateForSave(m, new ErrorsDto());
    if (errors.hasErrors()) {
      unifyError(errors);
      throw new ValidateException(errors);
    }

    m = preSave(m);
    m = dao.save(m);
    m = postSave(m);
    return m;
  }

  protected void unifyError(ErrorsDto errorsDto) {
    var errors = errorsDto.getErrors().iterator();
    ErrorDto generalError = null;
    List<String> errorFields = new ArrayList<>();

    while (errors.hasNext()) {
      var error = errors.next();
      if (StringUtils.isNotBlank(error.getErrorField())) {
        if (error.getErrorField().equals(Constants.GENERAL_ERROR_FIELD)) {
          generalError = error;
          errors.remove();
          continue;
        }
        errorFields.add(error.getErrorField());
      }
    }

    String message = "";
    if (!errorFields.isEmpty()) {
      message = "There is/are error(s) in fields: " + String.join(", ", errorFields).concat(".");
    }

    if (generalError == null) {
      generalError = new ErrorDto(Constants.GENERAL_ERROR_FIELD, message);
    } else {
      String key = generalError.getErrorKey();
      generalError =
          new ErrorDto(
              Constants.GENERAL_ERROR_FIELD,
              String.join(ErrorDto.TO_STRING_DELIM, Arrays.asList(key, message)));
    }

    errorsDto.getErrors().add(generalError);
  }
}
