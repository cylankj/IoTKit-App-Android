package com.cylan.annotation;

/**
 * Created by yzd on 17-1-13.
 */

public @interface DPTarget {

    Device[] target();

    Class<?> primary();//主要类型

    Class<?> generic() default Void.class;//泛型类型

    Class<?> parent();//继承的父类
}
