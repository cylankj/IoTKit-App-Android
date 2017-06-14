package com.cylan.jiafeigou.base;

import com.cylan.jiafeigou.base.wrapper.RequestResult;

/**
 * Created by yanzhendong on 2017/6/10.
 */

public class SetRequestResult implements RequestResult {
    @Override
    public int code() {
        return 0;
    }

    @Override
    public String message() {
        return null;
    }

    @Override
    public Object result() {
        return null;
    }
}
