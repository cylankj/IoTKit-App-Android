package com.cylan.jiafeigou.base.wrapper;

/**
 * Created by yanzhendong on 2017/6/10.
 */

public interface RequestResult<T> {

    int code();

    String message();

    T result();
}
