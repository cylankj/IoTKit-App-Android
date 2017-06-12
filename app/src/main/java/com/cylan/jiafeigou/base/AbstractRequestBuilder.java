package com.cylan.jiafeigou.base;

import rx.functions.Func1;

/**
 * Created by yanzhendong on 2017/6/10.
 */

public abstract class AbstractRequestBuilder<T> implements RequestBuilder<T> {
    protected RequestAdapter adapter;

    @Override
    public RequestAdapter adapter() {
        if (!checkParameter()) {
            throw new IllegalArgumentException("非法请求参数");
        }
        if (adapter == null) {
            adapter = new RequestAdapter();
        }
        adapter.request(this);
        return adapter;
    }

    @Override
    public RequestAdapter adapter(Func1 function) {

        return null;
    }

    @Override
    public void setAdapter(RequestAdapter adapter) {

    }

    protected abstract boolean checkParameter();

}
