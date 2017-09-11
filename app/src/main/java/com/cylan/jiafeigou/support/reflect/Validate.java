package com.cylan.jiafeigou.support.reflect;

/**
 * Created by hds on 17-9-11.
 */

class Validate {
    static void isTrue(final boolean expression, final String message, final Object... values) {
        if (expression == false) {
            throw new IllegalArgumentException(String.format(message, values));
        }
    }
}
