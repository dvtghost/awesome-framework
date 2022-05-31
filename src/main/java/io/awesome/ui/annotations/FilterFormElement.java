package io.awesome.ui.annotations;

import io.awesome.ui.enums.FilterFormSide;
import io.awesome.ui.enums.FormElementType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface FilterFormElement {
  FormElementType type() default FormElementType.TextField;

  String label() default "";

  FilterFormSide side() default FilterFormSide.LEFT;
}
