package io.awesome.mapper;

import io.awesome.enums.IAttributeEnum;
import io.awesome.mapper.converter.EnumSetToSelectDTOConverter;
import io.awesome.mapper.converter.FromStringConverter;
import io.awesome.mapper.converter.SelectDTOToEnumSetConverter;
import io.awesome.ui.components.SelectDto;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.ConstructorUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Component
@Deprecated
public class ModelMapperWrapper {
  private static final Logger logger = LoggerFactory.getLogger(ModelMapperWrapper.class);
  private ModelMapper modelMapper;

  public ModelMapperWrapper() {
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
  }

  public <S, D> D map(S source, D dest) {
    this.modelMapper.map(source, dest);
    return dest;
  }

  public <S, D> D map(S source, Class<D> destClass) {

    D dest;
    if (IAttributeEnum.class.isAssignableFrom(destClass)) {
      return this.modelMapper.map(source, destClass);
    }
    try {
      dest = ConstructorUtils.invokeConstructor(destClass, null);
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("Error create new dest from class %s", destClass.getName()), e);
    }
    this.modelMapper.map(source, dest);
    return dest;
  }

  private void configEnumAndSelectDTOConverters() {
    modelMapper
        .typeMap(SelectDto.class, TypedSet.class)
        .setConverter(new SelectDTOToEnumSetConverter(SelectDto.class, TypedSet.class, logger));
    modelMapper
        .typeMap(TypedSet.class, SelectDto.class)
        .setConverter(new EnumSetToSelectDTOConverter(TypedSet.class, SelectDto.class, logger));
  }
}
