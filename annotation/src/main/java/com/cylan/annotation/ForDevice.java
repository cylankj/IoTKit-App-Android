package com.cylan.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by cylan-hunt on 16-11-16.
 *
 */

@Retention(SOURCE)
@Target(FIELD)
public @interface ForDevice {

    Device[] device();

    /**
     * @return: 自动生成bean
     */
    String[] targetBeanName();
}