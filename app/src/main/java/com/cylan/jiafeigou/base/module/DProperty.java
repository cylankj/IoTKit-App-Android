package com.cylan.jiafeigou.base.module;

import com.cylan.ext.annotations.DPType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yanzhendong on 2017/3/24.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DProperty {
    Class<?> type();

    DPType dpType() default DPType.TYPE_FIELD;
}
