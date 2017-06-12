package com.cylan.jiafeigou.base;

import com.cylan.entity.JfgEvent;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by yanzhendong on 2017/6/10.
 */

public class SetRequestBuilder implements RequestBuilder<JfgEvent.RobotoSetDataRsp> {


    @Override
    public Observable<JfgEvent.RobotoSetDataRsp> process() {
        return null;
    }

    @Override
    public RequestAdapter adapter() {
        return null;
    }

    @Override
    public RequestAdapter adapter(Func1 function) {
        return null;
    }

    @Override
    public void setAdapter(RequestAdapter adapter) {

    }
}
