package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/2.
 */

public interface IDPTaskResult {

    <T extends IDPTaskResult> T setResultCode(int code);

    int getResultCode();

    <T extends IDPTaskResult> T setResultResponse(Object response);

    <R> R getResultResponse();

    <T extends IDPTaskResult> T setMessage(String response);

    String getMessage();


}
