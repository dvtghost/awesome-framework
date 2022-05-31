package io.awesome.dao;

import io.awesome.dto.FilterDto;
import io.awesome.dto.PagingDto;
import io.awesome.model.AbstractModel;
import io.awesome.model.BaseModel;
import io.awesome.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
public class AbstractDaoExtImpl<T extends BaseModel<ID>, ID> implements AbstractDaoExt<T> {
  @PersistenceContext protected EntityManager entityManager;

  public List<Predicate> addSearchCriteria(
      Root<T> root, CriteriaBuilder cb, List<FilterDto> filters) {
    List<Predicate> list = new ArrayList<>();
    for (FilterDto filter : filters) {
      addSearchCriteria(root, cb, list, filter);
    }
    return list;
  }

  private void addSearchCriteria(
      Root<T> root, CriteriaBuilder cb, List<Predicate> list, FilterDto filter) {
    // make join for filter
    AtomicReference<String> atomicFields = new AtomicReference<>(filter.getField());
    Path<T> join = makeJoin(root, atomicFields);
    String fields = atomicFields.get();

    switch (filter.getOperator()) {
      case EQ:
        list.add(cb.equal(join.get(fields), filter.getValue()[0]));
        break;
      case NE:
        list.add(cb.notEqual(join.get(fields), filter.getValue()[0]));
        break;
      case LT:
        list.add(cb.lt(join.get(fields), (Double) filter.getValue()[0]));
        break;
      case LE:
        list.add(cb.lessThanOrEqualTo(join.get(fields), (Double) filter.getValue()[0]));
        break;
      case GT:
        list.add(cb.gt(join.get(fields), (Double) filter.getValue()[0]));
        break;
      case GE:
        list.add(cb.greaterThanOrEqualTo(join.get(fields), (Double) filter.getValue()[0]));
        break;
      case LIKE:
        list.add(cb.like(join.get(fields), "%" + filter.getValue()[0] + "%"));
        break;
      case ILIKE:
        list.add(
            cb.like(
                cb.lower(join.get(fields)),
                "%" + String.valueOf(filter.getValue()[0]).toLowerCase(Locale.ROOT) + "%"));
        break;
      case IN:
        list.add(join.get(fields).in(Arrays.asList(filter.getValue())));
        break;
      case BEFORE:
        Object beforeValue = filter.getValue()[0];
        if (beforeValue == null) break;
        setupForAfterOrBeforeOperator(join, cb, list, filter, fields, false);
        break;
      case AFTER:
        Object afterValue = filter.getValue()[0];
        if (afterValue == null) break;
        setupForAfterOrBeforeOperator(join, cb, list, filter, fields, true);
        break;
      case BETWEEN:
        if (filter.getValue().length > 0) setupForBetweenOperator(join, cb, list, filter, fields);
        break;
      case ISNOTNULL:
        list.add(cb.isNotNull(join.get(fields)));
        break;
      case ISNULL:
        list.add(cb.isNull(join.get(fields)));
        break;
      default:
        setupForAndOrOrOperator(root, cb, list, filter, filter.getOperator());
        break;
    }
  }

  private void setupForAndOrOrOperator(
      Root<T> root,
      CriteriaBuilder cb,
      List<Predicate> list,
      FilterDto filter,
      FilterDto.Operator operator) {
    List<Predicate> predicates = addSearchCriteria(root, cb, finalizeFilters(filter.getFilters()));
    Predicate[] arrays = new Predicate[predicates.size()];
    list.add(
        operator.equals(FilterDto.Operator.AND)
            ? cb.and(predicates.toArray(arrays))
            : cb.or(predicates.toArray(arrays)));
  }

  private void setupForAfterOrBeforeOperator(
      Path<T> join,
      CriteriaBuilder cb,
      List<Predicate> list,
      FilterDto filter,
      String fields,
      boolean isAfter) {
    Object value = filter.getValue()[0];
    if (value instanceof LocalDate) {
      LocalDate afterOrBefore = (LocalDate) value;
      list.add(
          isAfter
              ? cb.greaterThanOrEqualTo(
                  join.get(fields), afterOrBefore.atStartOfDay().toLocalDate())
              : cb.lessThanOrEqualTo(join.get(fields), afterOrBefore.atStartOfDay().toLocalDate()));
    } else if (value instanceof LocalTime) {
      list.add(
          isAfter
              ? cb.greaterThanOrEqualTo(join.get(fields), (LocalTime) value)
              : cb.lessThanOrEqualTo(join.get(fields), (LocalTime) value));
    } else if (value instanceof LocalDateTime) {
      list.add(
          isAfter
              ? cb.greaterThanOrEqualTo(join.get(fields), ((LocalDateTime) value))
              : cb.lessThanOrEqualTo(join.get(fields), ((LocalDateTime) value)));
    } else if (value instanceof Date) {
      list.add(
          isAfter
              ? cb.greaterThanOrEqualTo(join.get(fields), (Date) value)
              : cb.lessThanOrEqualTo(join.get(fields), (Date) value));
    }
  }

  private void setupForBetweenOperator(
      Path<T> join, CriteriaBuilder cb, List<Predicate> list, FilterDto filter, String fields) {
    if (filter.getValue()[0] instanceof LocalDate) {
      Path<LocalDate> createdOn = join.get(fields);
      LocalDate from = (LocalDate) filter.getValue()[0];
      LocalDate to = (LocalDate) filter.getValue()[1];
      list.add(cb.between(createdOn, from, to));
    } else if (filter.getValue()[0] instanceof LocalDateTime) {
      Path<LocalDateTime> createdOn = join.get(fields);
      LocalDateTime localDateTimeFrom = (LocalDateTime) filter.getValue()[0];
      LocalDateTime localDateTimeTo = (LocalDateTime) filter.getValue()[1];
      list.add(cb.between(createdOn, localDateTimeFrom, localDateTimeTo));
    } else if (filter.getValue()[0] instanceof Date) {
      Path<Date> createdOn = join.get(fields);
      Date localDateTimeFrom = (Date) filter.getValue()[0];
      Date localDateTimeTo = (Date) filter.getValue()[1];
      list.add(cb.between(createdOn, localDateTimeFrom, localDateTimeTo));
    } else if (filter.getValue()[0] instanceof Double) {
      Path<Double> createdOn = join.get(fields);
      Double numberFrom = (Double) filter.getValue()[0];
      Double numberTo = (Double) filter.getValue()[1];
      list.add(cb.between(createdOn, numberFrom, numberTo));
    } else if (filter.getValue()[0] instanceof Integer) {
      Path<Integer> createdOn = join.get(fields);
      Integer numberFrom = (Integer) filter.getValue()[0];
      Integer numberTo = (Integer) filter.getValue()[1];
      list.add(cb.between(createdOn, numberFrom, numberTo));
    }
  }

  private Path<T> makeJoin(Root<T> root, AtomicReference<String> fields) {
    Path<T> join;
    if (fields.get().contains("##")) {
      String[] tokens = fields.get().split("##");
      fields.set(tokens[tokens.length - 1]);
      int level = tokens.length;
      Join<T, ? extends AbstractModel> innerJoin = root.join(tokens[0]);
      int i = 1;
      while (i < level - 1) {
        innerJoin = innerJoin.join(tokens[i++]);
      }
      join = (Path<T>) innerJoin;
    } else join = root;
    return join;
  }

  @Override
  public PagingDto<T> searchByFilters(Class<T> clazz, PagingDto<T> pagingDto, boolean fullInit) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<T> query = cb.createQuery(clazz);
    Root<T> root = query.from(clazz);
    query = query.select(root);
    return searchByFilters(clazz, pagingDto, fullInit, cb, root, query);
  }

  private void setupSortForFilter(
      PagingDto<T> pagingDto, CriteriaBuilder cb, Root<T> root, CriteriaQuery<T> query) {
    Sort sort = pagingDto.getPageable().getSort();
    List<Order> orders = new ArrayList<>();
    for (Sort.Order sortOrder : sort.get().collect(Collectors.toList())) {
      AtomicReference<String> atomicProperty = new AtomicReference<>(sortOrder.getProperty());
      Path<T> join = makeJoin(root, atomicProperty);
      String property = atomicProperty.get();
      orders.add(
          sortOrder.isAscending() ? cb.asc(join.get(property)) : cb.desc(join.get(property)));
    }
    query.orderBy(orders);
  }

  private void setupGroupByForFilter(PagingDto<T> pagingDto, Root<T> root, CriteriaQuery<T> query) {
    if (pagingDto.hasGroupBy()) {
      Path<?>[] joins = new Path[pagingDto.getGroupBy().size()];
      int i = 0;
      for (String field : pagingDto.getGroupBy()) {
        AtomicReference<String> fields = new AtomicReference<>(field);
        Path<T> join = makeJoin(root, fields);
        joins[i] = join;
        i++;
      }
      query.groupBy(joins);
    }
  }

  @Override
  public PagingDto<T> searchByFilters(
      Class<T> clazz,
      PagingDto<T> pagingDto,
      boolean fullInit,
      CriteriaBuilder cb,
      Root<T> root,
      CriteriaQuery<T> query) {
    try {
      if (!CollectionUtils.isEmpty(pagingDto.getFilters())) {
        List<Predicate> predicates =
            addSearchCriteria(root, cb, finalizeFilters(pagingDto.getFilters()));
        Predicate[] arrays = new Predicate[predicates.size()];
        query = query.where(cb.and(predicates.toArray(arrays)));
      }

      // make join for sort order
      setupSortForFilter(pagingDto, cb, root, query);

      // make join for group by
      setupGroupByForFilter(pagingDto, root, query);

      TypedQuery<T> typedQuery = entityManager.createQuery(query);
      if (pagingDto.getPageable().isPaged()) {
        long total = entityManager.createQuery(query).getResultList().size();
        pagingDto.setTotal(total);

        if (pagingDto.getPageable().getPageSize() == Integer.MAX_VALUE) {
          Pageable pageable = pagingDto.getPageable();
          pagingDto.setPageable(PageRequest.of(1, pageable.getPageSize() - 1, pageable.getSort()));
        }
        if (pagingDto.getPageable().getPageNumber() == Integer.MAX_VALUE) {
          int numberOfPages =
              (int) Math.ceil(((double) total) / pagingDto.getPageable().getPageSize());
          Pageable pageable = pagingDto.getPageable();
          pagingDto.setPageable(
              PageRequest.of(
                  Math.max(numberOfPages, 1), pageable.getPageSize(), pageable.getSort()));
        }
        int firstResult =
            (pagingDto.getPageable().getPageNumber() - 1) * pagingDto.getPageable().getPageSize();
        typedQuery.setFirstResult(firstResult);
        int maxResults = pagingDto.getPageable().getPageSize() + 1;
        typedQuery.setMaxResults(maxResults);
      }
      List<T> results = typedQuery.getResultList();
      if (pagingDto.getPageable().isPaged()
          && (results.size() > pagingDto.getPageable().getPageSize())) {
        pagingDto.setHasNext(true);
        if (!results.isEmpty()) {
          results = results.subList(0, results.size() - 1);
        }
      }
      if (!pagingDto.getPageable().isPaged()) pagingDto.setTotal((long) results.size());
      pagingDto.setResults(!CollectionUtils.isEmpty(results) ? results : new ArrayList<>());
      return pagingDto;
    } catch (Exception exception) {
      log.error(Constants.EXCEPTION_PREFIX, exception);
      throw new RuntimeException(exception);
    }
  }

  public Session getCurrentSession() {
    return (Session) this.entityManager.getDelegate();
  }
}
