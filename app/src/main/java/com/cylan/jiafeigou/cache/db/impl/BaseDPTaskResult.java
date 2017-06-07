package com.cylan.jiafeigou.cache.db.impl;

import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;

/**
 * Created by yanzhendong on 2017/3/2.
 */

public class BaseDPTaskResult implements IDPTaskResult {

    public static final BaseDPTaskResult SUCCESS = new BaseDPTaskResult().setResultCode(0);
    public static final BaseDPTaskResult ERROR = new BaseDPTaskResult().setResultCode(-1);

    private int resultCode = -1;
    private Object response;
    private String message;

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

    @Override
    public <T extends IDPTaskResult> T setMessage(String response) {
        this.message = response;
        return (T) this;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
