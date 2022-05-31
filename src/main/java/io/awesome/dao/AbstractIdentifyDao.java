package io.awesome.dao;

import io.awesome.model.BaseModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AbstractIdentifyDao<T extends BaseModel, ID>
    extends CrudRepository<T, ID>, AbstractDaoExt<T> {
  Slice<T> findAll(Pageable pageable);
}
