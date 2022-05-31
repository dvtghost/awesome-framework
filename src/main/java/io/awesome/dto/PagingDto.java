package io.awesome.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
@Setter
public class PagingDto<T> extends AbstractDto {
  public static int DEFAULT_RESULTS_PER_PAGE = 10;
  private Pageable pageable;
  private List<FilterDto> filters;
  private List<String> groupBy;
  private List<T> results;
  private Long total;
  private boolean hasNext;

  public PagingDto() {}

  public PagingDto(Pageable pageable, List<FilterDto> filters, List<T> results) {
    this.pageable = pageable;
    this.filters = filters;
    this.results = results;
  }

  public PagingDto(
      Pageable pageable, List<FilterDto> filters, List<String> groupBy, List<T> results) {
    this.pageable = pageable;
    this.filters = filters;
    this.groupBy = groupBy;
    this.results = results;
  }

  public PagingDto(Pageable pageable, List<FilterDto> filters, List<T> results, boolean hasNext) {
    this.pageable = pageable;
    this.filters = filters;
    this.results = results;
    this.hasNext = hasNext;
  }

  public boolean hasGroupBy() {
    return this.groupBy != null && !this.groupBy.isEmpty();
  }
}
