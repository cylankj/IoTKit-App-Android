package com.cylan.jiafeigou.n.task;

import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 意见反馈
 * Created by hds on 17-6-7.
 */

public class FetchSuggestionTask implements Action1<Object> {

    @Override
    public void call(Object o) {
        Observable.just("FetchSuggestionTask")
                .subscribeOn(Schedulers.newThread())
                .flatMap(s -> {
                    int req = BaseApplication.getAppComponent().getCmd().getFeedbackList();
                    return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetFeedBackRsp.class);
                })
                .subscribeOn(Schedulers.io())
                .subscribe(getFeedBackRsp -> {
                    AppLogger.d("FetchSuggestionTask rsp");
                }, AppLogger::e);
    }

}
