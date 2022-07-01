package io.awesome.ui.models;

import io.awesome.dto.PagingDto;
import io.awesome.exception.SystemException;
import io.awesome.util.DeepCopyBeanUtils;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DataTableSource<T> {
  private final Class<T> entityClazz;
  private final Function<Pageable, PagingDto<T>> recordsPerPageFnc;
  private final PagingDto<T> records;

  public DataTableSource(
      final Class<T> entityClazz, final Function<Pageable, PagingDto<T>> recordsPerPageFnc) {
    this.records = new PagingDto<>();
    this.entityClazz = entityClazz;
    this.recordsPerPageFnc = recordsPerPageFnc;
  }

  public PagingDto<T> load(final Pageable pageable) {
    PagingDto<T> results = recordsPerPageFnc.apply(pageable);
    records.setPageable(pageable);
    List<T> entities = new ArrayList<>();
    for (T result : results.getResults()) {
      T entity = createInstance(entityClazz);
      DeepCopyBeanUtils.copyProperties(result, entity);
      entities.add(entity);
    }
    records.setResults(entities);
    return records;
  }

  public PagingDto<T> getRecords() {
    return records;
  }

  private <N> N createInstance(Class<N> clazz) {
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      // TODO: adding error code
      throw new SystemException(e);
    }
  }
}
