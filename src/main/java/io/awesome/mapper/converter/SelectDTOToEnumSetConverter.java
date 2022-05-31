package io.awesome.mapper.converter;

import io.awesome.mapper.TypedSet;
import io.awesome.ui.components.SelectDto;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;

public class SelectDTOToEnumSetConverter<T extends Enum<T>>
    extends BaseConverter<SelectDto, TypedSet<T>> {
  public SelectDTOToEnumSetConverter(
      Class<SelectDto> sourceClass, Class<TypedSet<T>> destClass, Logger logger) {
    super(sourceClass, destClass, logger);
  }

  @Override
  public TypedSet<T> doConvert(SelectDto source, final TypedSet<T> dest) {
    logger.info("convert selectDTO to Enum");
    if (source == null || CollectionUtils.isEmpty(source.getSelected())) {
      return dest;
    }
    source
        .getSelected()
        .forEach(
            i ->
                Arrays.stream(dest.getAllPossibleItemValues())
                    .filter(tEnum -> tEnum.name().equalsIgnoreCase(i.getName()))
                    .findFirst()
                    .ifPresent(tEnum -> dest.add((T) tEnum)));
    return dest;
  }

  @Override
  public Pair<SelectDto, TypedSet<T>> preConvert(SelectDto source, TypedSet<T> dest) {
    if (source == null) {
      source = new SelectDto();
    }
    if (dest == null) {
      throw new RuntimeException(
          "SelectDTO to EnumSet Converter, dest TypedSet need to be initialized");
    } else {
      dest.clear();
    }
    return Pair.of(source, dest);
  }
}
