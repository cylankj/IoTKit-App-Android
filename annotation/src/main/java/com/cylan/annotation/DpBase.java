package com.cylan.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by cylan-hunt on 16-11-23.
 */

@Retention(CLASS)
@Target({ElementType.TYPE})
public @interface DpBase {
}
