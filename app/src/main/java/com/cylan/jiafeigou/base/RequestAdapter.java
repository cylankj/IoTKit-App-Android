package com.cylan.jiafeigou.base;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/6/10.
 */

public class RequestAdapter implements RequestAPI {
    private List<RequestBuilder> builders = new ArrayList<>();

    public RequestAdapter request(RequestBuilder builder) {
        if (builder != null && !builders.contains(builder)) {
            builders.add(builder);
        }
        return this;
    }


    @Override
    public GetRequestBuilder get() {
//        GetRequestBuilder getRequestBuilder = RequestManager.getInstance().get();
//        getRequestBuilder.setAdapter(this);
//        return getRequestBuilder;
        return null;
    }

    @Override
    public UploadRequestBuilder upload() {
        return null;
    }

    @Override
    public SetRequestBuilder set() {
        return null;
    }

    @Override
    public DeleteRequestBuilder delete() {
        return null;
    }


    public Observable<?> execute() {
        List<Observable<?>> concat = new ArrayList<>();
        for (RequestBuilder builder : builders) {
            concat.add(builder.process());
        }
        return Observable.concat(concat).last();
    }
}
