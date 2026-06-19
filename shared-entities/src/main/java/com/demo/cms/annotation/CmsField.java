package com.demo.cms.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define schema metadata for a field in a CMS Component.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CmsField {
    String displayName();
    String type() default "string"; // e.g., "string", "text", "boolean", "array_string"
    boolean required() default false;
    String placeholder() default "";
}
