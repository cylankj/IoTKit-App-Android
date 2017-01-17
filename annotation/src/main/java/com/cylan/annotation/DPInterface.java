package com.cylan.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.cylan.annotation.DPTarget.DATAPOINT;

/*
 *  @项目名：  APT 
 *  @包名：    com.annotation
 *  @文件名:   DPInterface
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/13 22:22
 *  @描述：    TODO
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DPInterface {
    DPTarget parent() default DATAPOINT;//Void父类即为DataPoint,因为无法循环引用的原因,无法直接写app模块中的DataPoint

    String name();
}
