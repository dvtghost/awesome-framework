package io.awesome.mapper.converter;

import io.awesome.enums.IAttributeEnum;
import io.awesome.ui.components.SelectDto;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EnumToSelectDTOConverter<T extends Enum<T>> extends BaseConverter<T, SelectDto> {

  public EnumToSelectDTOConverter(Class<T> sourceClass, Class<SelectDto> destClass, Logger logger) {
    super(sourceClass, destClass, logger);
  }

  @Override
  public SelectDto doConvert(T source, SelectDto dest) {
    if (dest == null) {
      throw new RuntimeException("Dest selectDTO is not preConverted");
    }
    if (source == null) {
      return dest;
    }
    IAttributeEnum<?, T> iAttributeEnum = (IAttributeEnum<?, T>) source;
    dest.setSelected(
        Collections.singletonList(
            new SelectDto.SelectItem(
                iAttributeEnum.getValue(),
                iAttributeEnum.getName(),
                iAttributeEnum.getLabel(),
                iAttributeEnum.getColor())));
    return dest;
  }

  @Override
  public Pair<T, SelectDto> preConvert(T source, SelectDto dest) {
    List<SelectDto.SelectItem> allPossibleItems =
        Arrays.stream(sourceClass.getEnumConstants())
            .map(e -> (IAttributeEnum<?, T>) e)
            .map(
                e ->
                    new SelectDto.SelectItem(e.getValue(), e.getName(), e.getLabel(), e.getColor()))
            .collect(Collectors.toList());

    dest = new SelectDto(allPossibleItems, new ArrayList<>());

    if (source == null) {
      return Pair.of(null, dest);
    }

    if (!(source instanceof IAttributeEnum)) {
      throw new RuntimeException("Wrong source Type, must be Iattribute<?, T?");
    }

    return Pair.of(source, dest);
  }
}
