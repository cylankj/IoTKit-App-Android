package com.cylan.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 *  @项目名：  APT 
 *  @包名：    com.annotation
 *  @文件名:   DPMessage
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/13 22:19
 *  @描述：    TODO
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface DPMessage {
    String name() default "";

    Class<?> type();

    DPType dpType() default DPType.TYPE_FIELD;

    DPTarget[] target() default {DPTarget.CAMERA};
}
