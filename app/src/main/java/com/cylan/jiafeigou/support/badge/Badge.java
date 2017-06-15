package com.cylan.jiafeigou.support.badge;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hds on 17-6-14.
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
}