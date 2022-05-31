package io.awesome.mapper.converter;

import org.slf4j.Logger;

public class FromStringConverter<D extends Number> extends BaseConverter<String, D> {
  public FromStringConverter(Class<String> sourceClass, Class<D> destClass, Logger logger) {
    super(sourceClass, destClass, logger);
  }

  @Override
  public D doConvert(String source, D dest) {
    if (source == null) {
      logger.debug("Source is null, don't touch dest");
      return dest;
    }
    if (destClass.equals(Integer.class)) {
      return (D) Integer.valueOf(source);
    }
    if (destClass.equals(Long.class)) {
      return (D) Long.valueOf(source);
    }
    if (destClass.equals(Double.class)) {
      return (D) Double.valueOf(source);
    }
    if (destClass.equals(Float.class)) {
      return (D) Float.valueOf(source);
    }

    throw new RuntimeException("Only support String to Integer, Long, Double, Float");
  }
}
