package com.cylan.jiafeigou.support.badge;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hds on 17-6-9.
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Badge {

    /**
     * 包名,比直接 类的字符串好.(考虑java混淆)
     *
     * @return
     */
    String parentTag();

    /**
     * 新功能,需要在初始化的时候,标记为红点.
     *
     * @return
     */
    boolean asRefresh() default false;
}