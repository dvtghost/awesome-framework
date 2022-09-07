package io.awesome.ui.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceTag {
    Type type() default Type.TAG;

    boolean ignore() default false;

    enum Type {
        ID,
        TAG
    }
}