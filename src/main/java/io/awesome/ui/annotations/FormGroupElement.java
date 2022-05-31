package io.awesome.ui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface FormGroupElement {
  String label() default "Default Form Group Label";

  int colspan() default 1;

  boolean editable() default true;

  boolean isFormEdit() default false;

  boolean isCollapse() default true;

  boolean isDefaultCollapse() default true;
}
