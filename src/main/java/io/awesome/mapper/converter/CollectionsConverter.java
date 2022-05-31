package io.awesome.mapper.converter;

import org.slf4j.Logger;

import java.util.Collection;

public class CollectionsConverter<ES, ED, S extends Collection<ES>, D extends Collection<ED>>
    extends BaseConverter<S, D> {
  public CollectionsConverter(Class<S> sourceClass, Class<D> destClass, Logger logger) {
    super(sourceClass, destClass, logger);
  }

  @Override
  public D doConvert(S source, D dest) {
    return null;
  }
}
