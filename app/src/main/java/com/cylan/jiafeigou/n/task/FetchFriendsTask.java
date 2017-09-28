package com.cylan.jiafeigou.n.task;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.mine.MineFriendsFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.CacheObject;
import com.cylan.jiafeigou.support.badge.TreeHelper;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 好友列表
 * Created by hds on 17-6-7.
 */

public class FetchFriendsTask implements Action1<Object> {
    @Override
    public void call(Object o) {
        AppLogger.d("需要查询db");
        Observable.zip(getFriendListObservable(), getFriendReqListObservable(), (getFriendList, getAddReqList) -> {
            ArrayList<JFGFriendAccount> fList = BaseApplication.getAppComponent().getSourceManager().getFriendsList();
            ArrayList<JFGFriendRequest> fReqList = BaseApplication.getAppComponent().getSourceManager().getFriendsReqList();
            TreeHelper helper = BaseApplication.getAppComponent().getTreeHelper();
            //需要替换数据库
            try {
                if (fReqList != null) {
                    TreeNode node = helper.findTreeNodeByName(MineFriendsFragment.class.getSimpleName());
                    node.setCacheData(new CacheObject().setCount(ListUtils.getSize(fReqList))
                            .setObject(fReqList));
                }
            } catch (Exception e) {
                AppLogger.e(e.getMessage());
            }
            RxBus.getCacheInstance().postSticky(new RxEvent.AllFriendsRsp());
            RxBus.getCacheInstance().postSticky(new RxEvent.InfoUpdate());

            AppLogger.w("FetchFriendsTask rsp: " + new Gson().toJson(fList) + "h" + helper);
            AppLogger.w("FetchFriendsTask rsp: " + new Gson().toJson(fReqList));
            return null;
        })
                .timeout(10, TimeUnit.SECONDS)
                .subscribe(ret -> {
                }, AppLogger::e);
    }

    /**
     * 好友列表
     *
     * @return
     */
    private Observable<RxEvent.GetFriendList> getFriendListObservable() {
        return Observable.just("goGet")
                .subscribeOn(Schedulers.io())
                .flatMap(s -> {
                    BaseApplication.getAppComponent().getCmd().getFriendList();
                    return RxBus.getCacheInstance().toObservable(RxEvent.GetFriendList.class);
                });
    }

    /**
     * 好友请求列表
     *
     * @return
     */
    private Observable<RxEvent.GetAddReqList> getFriendReqListObservable() {
        return Observable.just("goGet")
                .subscribeOn(Schedulers.io())
                .flatMap(s -> {
                    BaseApplication.getAppComponent().getCmd().getFriendRequestList();
                    return RxBus.getCacheInstance().toObservable(RxEvent.GetAddReqList.class);
                });
    }
}
