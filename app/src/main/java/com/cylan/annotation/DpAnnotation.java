package com.cylan.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by cylan-hunt on 16-11-15.
 */

@Retention(CLASS)
@Target(FIELD)
public @interface DpAnnotation {
    int msgId();

    Class<?> clazz();
}
