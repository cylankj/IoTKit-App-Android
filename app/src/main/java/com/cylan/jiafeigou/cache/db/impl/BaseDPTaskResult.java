package com.cylan.jiafeigou.cache.db.impl;

import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;

/**
 * Created by yanzhendong on 2017/3/2.
 */

public class BaseDPTaskResult implements IDPTaskResult {

    private int resultCode;
    private Object response;

    @Override
    public <T extends IDPTaskResult> T setResultCode(int code) {
        this.resultCode = code;
        return (T) this;
    }

    @Override
    public int getResultCode() {
        return resultCode;
    }

    @Override
    public <T extends IDPTaskResult> T setResultResponse(Object response) {
        this.response = response;
        return (T) this;
    }

    @Override
    public <R> R getResultResponse() {
        return (R) this.response;
    }
}
