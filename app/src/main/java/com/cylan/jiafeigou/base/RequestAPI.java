package com.cylan.jiafeigou.base;

/**
 * Created by yanzhendong on 2017/6/10.
 */

public interface RequestAPI {

    GetRequestBuilder get();

    UploadRequestBuilder upload();

    SetRequestBuilder set();

    DeleteRequestBuilder delete();
}
