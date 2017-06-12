package com.cylan.jiafeigou.base;

import com.cylan.jiafeigou.base.wrapper.RequestResult;

import java.util.List;

/**
 * Created by yanzhendong on 2017/6/10.
 */

public class GetRequestResult<T> implements RequestResult<List<T>> {
    @Override
    public int code() {
        return 0;
    }

    @Override
    public String message() {
        return null;
    }

    @Override
    public List<T> result() {
        return null;
    }
}
