package io.awesome.dao;

import io.awesome.model.AbstractModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AbstractDao<T extends AbstractModel>
    extends AbstractIdentifyDao<T, String>, AbstractDaoExt<T> {
  Slice<T> findAll(Pageable pageable);
}
