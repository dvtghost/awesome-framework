package io.awesome.ui.components.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResource implements Serializable {
  private int page;
  private int limit;
  private boolean hasNext;

  private PaginationResource(Builder builder) {
    this.page = builder.page;
    this.limit = builder.limit;
    this.hasNext = builder.hasNext;
  }

  public static Builder newBuilder() {
    return Builder.create();
  }

  public PaginationResource first() {
    return PaginationResource.newBuilder().setPage(1).setLimit(limit).build();
  }

  public PaginationResource previous() {
    if (!hasPrevious()) {
      return this;
    }

    return PaginationResource.newBuilder().setPage(page - 1).setLimit(limit).build();
  }

  public PaginationResource next() {
    if (!isHasNext()) {
      return this;
    }
    return PaginationResource.newBuilder().setPage(page + 1).setLimit(limit).build();
  }

  public PaginationResource last() {
    if (!isHasNext()) {
      return this;
    }
    return PaginationResource.newBuilder().setPage(Integer.MAX_VALUE).setLimit(limit).build();
  }

  public boolean hasPrevious() {
    return page > 1;
  }

  @NoArgsConstructor
  public static class Builder {
    public boolean hasNext;
    private int page;
    private int limit;

    private static Builder create() {
      return new Builder();
    }

    public Builder setPage(int page) {
      this.page = page;
      return this;
    }

    public Builder setLimit(int limit) {
      this.limit = limit;
      return this;
    }

    public Builder setHasNext(boolean hasNext) {
      this.hasNext = hasNext;
      return this;
    }

    public PaginationResource build() {
      return new PaginationResource(this);
    }
  }
}
