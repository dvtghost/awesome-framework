package io.awesome.mapper.converter;

import io.awesome.enums.IAttributeEnum;
import io.awesome.ui.components.SelectDto;
import io.awesome.util.JsonObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;

public class SelectDTOToEnumConverter<T extends Enum<T>> extends BaseConverter<SelectDto, T> {

  private IAttributeEnum<?, T>[] allPossibleDestEnums;

  public SelectDTOToEnumConverter(Class<SelectDto> sourceClass, Class<T> destClass, Logger logger) {
    super(sourceClass, destClass, logger);
    try {
      this.allPossibleDestEnums = (IAttributeEnum<?, T>[]) destClass.getEnumConstants();
    } catch (Exception e) {
      logger.error("wrong cash from IAttributeEnum to Enum");
      throw new RuntimeException(
          String.format(
              "wrong cash from IAttributeEnum %s to Enum %s",
              allPossibleDestEnums.getClass().getComponentType().getName(),
              this.destClass.getName()));
    }
  }

  @Override
  public Pair<SelectDto, T> preConvert(SelectDto source, T dest) {
    if (source == null) {
      source = new SelectDto();
    }
    return Pair.of(source, dest);
  }

  @Override
  public T doConvert(SelectDto source, T dest) {
    logger.info("convert selectDTO to Enum");
    if (source == null) {
      throw new RuntimeException("source SelectDto is null");
    }
    String name =
        !CollectionUtils.isEmpty(source.getSelected()) && source.getSelected().get(0) != null
            ? source.getSelected().get(0).getName()
            : null;
    IAttributeEnum<?, T> item =
        Arrays.stream(allPossibleDestEnums)
            .filter(e -> e.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    if (item == null) {
      return null;
    }
    if (item.getClass().isAssignableFrom(destClass)) {
      return (T) item;
    }
    throw new RuntimeException(
        String.format(
            "Can't convert SelectDTO %s to dest %s",
            JsonObjectMapper.toJson(source, logger), destClass));
  }
}
