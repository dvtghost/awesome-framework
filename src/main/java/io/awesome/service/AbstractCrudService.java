package io.awesome.service;

import io.awesome.dao.AbstractDao;
import io.awesome.model.AbstractModel;
import io.awesome.ui.models.Editable;
import io.awesome.ui.models.Listable;

public abstract class AbstractCrudService<M extends AbstractModel, L extends Listable<E>, E extends Editable>
    extends AbstractIdentifyCrudService<String, M, L, E> {

  public AbstractCrudService(Class<M> modelClass, AbstractDao<M> dao) {
    super(modelClass, dao);
  }

}
