package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/22.
 */

public interface IDPTaskException {

    int getErrorCode();

    <T extends IDPTaskException> T setErrorCode(int errorCode);

    <T extends IDPTaskException> T setErrorDescription(String error);

    String getErrorDescription();

}
