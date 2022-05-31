package io.awesome.mapper.converter;

import io.awesome.exception.BaseException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.slf4j.Logger;

public abstract class BaseConverter<S, D> implements Converter<S, D> {

  protected Class<S> sourceClass;
  protected Class<D> destClass;
  protected Logger logger;

  public BaseConverter(Class<S> sourceClass, Class<D> destClass, Logger logger) {
    this.sourceClass = sourceClass;
    this.destClass = destClass;
    this.logger = logger;
  }

  @SneakyThrows
  @Override
  public D convert(MappingContext<S, D> mappingContext) {
    D destination = mappingContext.getDestination();
    S source = mappingContext.getSource();
    return convert(source, destination);
  }

  public D convert(S source, D destination) {
    try {
      Pair<S, D> pair = preConvert(source, destination);
      return doConvert(pair.getLeft(), pair.getRight());
    } catch (Exception e) {
      throw new BaseException(
              String.format(
                      "Error converting with mappingContext %s ",
                      e.getLocalizedMessage()),
              e);
    }
  }

  public Pair<S, D> preConvert(S source, D dest) {
    this.logger.info(String.format("Converter - default preConvert %s - %s", source, dest));
    return Pair.of(source, dest);
  }

  public abstract D doConvert(S source, D dest);
}
