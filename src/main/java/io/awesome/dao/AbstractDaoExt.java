package io.awesome.dao;

import io.awesome.dto.FilterDto;
import io.awesome.dto.PagingDto;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public interface AbstractDaoExt<T> {
  default List<FilterDto> finalizeFilters(List<FilterDto> filters) {
    return filters;
  }

  PagingDto<T> searchByFilters(Class<T> clazz, PagingDto<T> pagingDto, boolean fullInit);

  PagingDto<T> searchByFilters(
      Class<T> clazz,
      PagingDto<T> pagingDto,
      boolean fullInit,
      CriteriaBuilder cb,
      Root<T> root,
      CriteriaQuery<T> additionalPredicate);
}
