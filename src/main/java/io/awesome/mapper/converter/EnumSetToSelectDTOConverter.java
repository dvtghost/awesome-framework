package io.awesome.mapper.converter;

import io.awesome.enums.IAttributeEnum;
import io.awesome.mapper.TypedSet;
import io.awesome.ui.components.SelectDto;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnumSetToSelectDTOConverter<T extends Enum<T>>
    extends BaseConverter<TypedSet<T>, SelectDto> {
  public EnumSetToSelectDTOConverter(
      Class<TypedSet<T>> sourceClass, Class<SelectDto> destClass, Logger logger) {
    super(sourceClass, destClass, logger);
  }

  @Override
  public Pair<TypedSet<T>, SelectDto> preConvert(TypedSet<T> source, SelectDto dest) {
    if (source == null) {
      throw new RuntimeException("source of type TypedSet need to be initialized ahead");
    }
    if (dest == null) {
      dest = new SelectDto();
    }
    return Pair.of(source, dest);
  }

  @Override
  public SelectDto doConvert(TypedSet<T> source, SelectDto dest) {
    List<SelectDto.SelectItem> selectedItems = new ArrayList<>();
    if (!CollectionUtils.isEmpty(source)) {
      source.forEach(
          i -> {
            IAttributeEnum<?, ?> e = (IAttributeEnum<?, ?>) i;
            selectedItems.add(
                new SelectDto.SelectItem(e.getValue(), e.getName(), e.getLabel(), e.getColor()));
          });
    }
    List<SelectDto.SelectItem> allItems = new ArrayList<>();
    Arrays.stream(source.getComponentType().getEnumConstants())
        .forEach(
            i -> {
              IAttributeEnum<?, ?> e = (IAttributeEnum<?, ?>) i;
              allItems.add(
                  new SelectDto.SelectItem(e.getValue(), e.getName(), e.getLabel(), e.getColor()));
            });
    dest.setItems(allItems);
    dest.setSelected(selectedItems);
    return dest;
  }
}
