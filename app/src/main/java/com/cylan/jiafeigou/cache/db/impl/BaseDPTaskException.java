package com.cylan.jiafeigou.cache.db.impl;

import com.cylan.jiafeigou.cache.db.view.IDPTaskException;

/**
 * Created by yanzhendong on 2017/3/22.
 */

public class BaseDPTaskException extends RuntimeException implements IDPTaskException {
    private int errorCode;
    private String error;

    @Override
    public int getErrorCode() {
        return this.errorCode;
    }

    @Override
    public <T extends IDPTaskException> T setErrorCode(int errorCode) {
        this.errorCode = errorCode;
        return (T) this;
    }

    public BaseDPTaskException(int errorCode, String error) {
        this.errorCode = errorCode;
        this.error = error;
    }

    @Override
    public <T extends IDPTaskException> T setErrorDescription(String error) {
        this.error = error;
        return (T) this;
    }

    @Override
    public String getErrorDescription() {
        return this.error;
    }
}
