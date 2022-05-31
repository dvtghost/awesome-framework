package io.awesome.enums.converter;

import io.awesome.enums.IAttributeEnum;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface EnumPersistentConverter {
  Class<? extends IAttributeEnum> enumClass() default IAttributeEnum.class;

  EnumPersistentType sqlType() default EnumPersistentType.BYTE;
}
