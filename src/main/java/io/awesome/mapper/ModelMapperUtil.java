package io.awesome.mapper;

import io.awesome.mapper.converter.EnumSetToSelectDTOConverter;
import io.awesome.mapper.converter.FromStringConverter;
import io.awesome.mapper.converter.SelectDTOToEnumSetConverter;
import io.awesome.ui.components.SelectDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModelMapperUtil {
  private static final Logger logger = LoggerFactory.getLogger(ModelMapperUtil.class);
  private static volatile ModelMapper modelMapper;

  public static synchronized ModelMapper modelMapper() {
    if (modelMapper == null) {
      logger.info("Initializing modelMapper");
      modelMapper = new ModelMapper();
      modelMapper
          .typeMap(String.class, Number.class)
          .setConverter(new FromStringConverter<>(String.class, Number.class, logger));

      modelMapper.typeMap(ArrayList.class, List.class).setConverter(MappingContext::getSource);
      modelMapper.typeMap(Set.class, Set.class).setConverter(MappingContext::getSource);
      modelMapper.typeMap(TypedSet.class, TypedSet.class).setConverter(MappingContext::getSource);

      configEnumAndSelectDTOConverters();
    }
    return modelMapper;
  }

  private static void configEnumAndSelectDTOConverters() {
    modelMapper
        .typeMap(SelectDto.class, TypedSet.class)
        .setConverter(new SelectDTOToEnumSetConverter(SelectDto.class, TypedSet.class, logger));
    modelMapper
        .typeMap(TypedSet.class, SelectDto.class)
        .setConverter(new EnumSetToSelectDTOConverter(TypedSet.class, SelectDto.class, logger));
  }
}
