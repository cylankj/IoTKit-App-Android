package com.cylan.jiafeigou.n.task;

import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Observable;
import rx.functions.Action1;

/**
 * 意见反馈
 * Created by hds on 17-6-7.
 */

public class FetchFeedbackTask implements Action1<Object> {

    @Override
    public void call(Object o) {
        Observable.create(subscriber -> {
            try {
                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribe(getFeedBackRsp -> BaseApplication.getAppComponent().getCmd().getFeedbackList(), AppLogger::e);
    }

}
