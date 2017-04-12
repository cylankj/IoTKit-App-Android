package com.cylan.jiafeigou.base.injector.lifecycle;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Created by yanzhendong on 2017/4/12.
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Presenter {
    String value() default "";
}
