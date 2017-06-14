package com.cylan.jiafeigou.n.task;

import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.mine.HomeMineHelpFragment;
import com.cylan.jiafeigou.n.view.mine.MineFriendsFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.TreeHelper;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

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
        Observable.just("FetchFeedbackTask")
                .subscribeOn(Schedulers.newThread())
                .flatMap(s -> {
                    int req = BaseApplication.getAppComponent().getCmd().getFeedbackList();
                    return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetFeedBackRsp.class);
                })
                .subscribeOn(Schedulers.io())
                .subscribe(getFeedBackRsp -> {
                    AppLogger.d("FetchFeedbackTask rsp");
                    ArrayList<JFGFeedbackInfo> list = BaseApplication.getAppComponent().getSourceManager().getNewFeedbackList();
                    TreeHelper helper = BaseApplication.getAppComponent().getTreeHelper();
                    TreeNode node = helper.findTreeNodeByName(HomeMineHelpFragment.class.getSimpleName());
                    node.setData(list);
                    RxBus.getCacheInstance().postSticky(new RxEvent.AllFriendsRsp());
                }, AppLogger::e);
    }

}
