package com.cylan.jiafeigou.n.task;

import com.cylan.jiafeigou.module.Command;
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
                subscriber.onNext("good");
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribe(getFeedBackRsp -> Command.getInstance().getFeedbackList(), AppLogger::e);
    }

}
