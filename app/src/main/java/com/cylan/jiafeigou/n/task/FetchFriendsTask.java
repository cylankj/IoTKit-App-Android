package com.cylan.jiafeigou.n.task;

import android.util.Pair;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;

import java.util.ArrayList;

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
        Observable.zip(getFriendListObservable(), getFriendReqListObservable(),
                Pair::new)
                .subscribeOn(Schedulers.io())
                .subscribe(pair -> {
                    ArrayList<JFGFriendAccount> fList = pair.first.arrayList;
                    ArrayList<JFGFriendRequest> fReqList = pair.second.arrayList;
                    BaseApplication.getAppComponent().getSourceManager().setPairFriends(new Pair<>(fList, fReqList));
                    RxBus.getCacheInstance().post(new RxEvent.AllFriendsRsp());
                    AppLogger.d("FetchFriendsTask rsp");
                    throw new RxEvent.HelperBreaker("yes, job done!");
                }, AppLogger::e);
    }

    /**
     * 好友列表
     *
     * @return
     */
    private Observable<RxEvent.GetFriendList> getFriendListObservable() {
        return Observable.just("goGet")
                .subscribeOn(Schedulers.newThread())
                .flatMap(s -> {
                    BaseApplication.getAppComponent().getCmd().getFriendList();
                    return RxBus.getCacheInstance().toObservable(RxEvent.GetFriendList.class)
                            .filter(ret -> !ListUtils.isEmpty(ret.arrayList));
                });
    }

    /**
     * 好友请求列表
     *
     * @return
     */
    private Observable<RxEvent.GetAddReqList> getFriendReqListObservable() {
        return Observable.just("goGet")
                .subscribeOn(Schedulers.newThread())
                .flatMap(s -> {
                    BaseApplication.getAppComponent().getCmd().getFriendRequestList();
                    return RxBus.getCacheInstance().toObservable(RxEvent.GetAddReqList.class)
                            .filter(ret -> !ListUtils.isEmpty(ret.arrayList));
                });
    }
}
