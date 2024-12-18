package com.assignment2.parser;

import java.lang.annotation.*;

/**
 * Annotation to map CSV columns to class fields.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CSVField {
    String columnName();

    boolean required() default true;

    String defaultValue() default "";
}
