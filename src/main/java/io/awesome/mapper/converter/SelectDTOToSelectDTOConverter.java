package io.awesome.mapper.converter;

import io.awesome.ui.components.SelectDto;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SelectDTOToSelectDTOConverter
    extends BaseConverter<SelectDto, SelectDto> {
  public SelectDTOToSelectDTOConverter(
      Class<SelectDto> sourceClass, Class<SelectDto> destClass, Logger logger) {
    super(sourceClass, destClass, logger);
  }

  @Override
  public Pair<SelectDto, SelectDto> preConvert(SelectDto source, SelectDto dest) {
    if (source == null) {
      throw new RuntimeException("source of type TypedSet need to be initialized ahead");
    }
    dest = new SelectDto();
    return Pair.of(source, dest);
  }

  @Override
  public SelectDto doConvert(SelectDto source, SelectDto dest) {
    dest.getItems().addAll(source.getItems());
    dest.getSelected().addAll(source.getSelected());
    return dest;
  }
}
