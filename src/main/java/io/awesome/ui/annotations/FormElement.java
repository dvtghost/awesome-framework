package io.awesome.ui.annotations;

import io.awesome.ui.enums.FormElementType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface FormElement {
  FormElementType type() default FormElementType.TextField;

  String label() default "";

  String formSectionHeader() default "";

  String formItemGroup() default "";

  boolean enable() default true;

  boolean readOnly() default false;

  int colspan() default 2;

  boolean required() default false;

  String forFieldRange() default "";

  boolean isClearButton() default true;

  String acceptedFileTypes() default "";

  boolean isRadioButtonGroupVertical() default false;

  String pattern() default "";

  String patternExample() default "";
}
