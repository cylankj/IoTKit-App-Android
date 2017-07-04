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
        }).subscribeOn(Schedulers.newThread())
                .flatMap(s -> {
                    int req = BaseApplication.getAppComponent().getCmd().getFeedbackList();
                    return RxBus.getCacheInstance().toObservable(RxEvent.GetFeedBackRsp.class);
                })
                .subscribeOn(Schedulers.io())
                .subscribe(getFeedBackRsp -> {
                    AppLogger.d("FetchFeedbackTask rsp");
//                    ArrayList<JFGFeedbackInfo> list = BaseApplication.getAppComponent().getSourceManager().getNewFeedbackList();
//                    TreeHelper helper = BaseApplication.getAppComponent().getTreeHelper();
//                    TreeNode node = helper.findTreeNodeByName(HomeMineHelpFragment.class.getSimpleName());
//                    node.setCacheData(new CacheObject().setCount(ListUtils.getSize(list)).setObject(list));
//                    RxBus.getCacheInstance().postSticky(new RxEvent.InfoUpdate());
                    throw new RxEvent.HelperBreaker("结束本地调用");
                }, AppLogger::e);
    }

}
