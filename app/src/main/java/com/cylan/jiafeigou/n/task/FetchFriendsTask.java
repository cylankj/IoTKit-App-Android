package com.cylan.jiafeigou.n.task;

import android.util.Pair;

import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.module.FriendBean;
import com.cylan.jiafeigou.cache.db.module.FriendsReqBean;
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
import java.util.List;
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
        Observable.zip(getFriendListObservable(), getFriendReqListObservable(),
                Pair::new)
                .subscribeOn(Schedulers.io())
                .timeout(10, TimeUnit.SECONDS)
                .subscribe(ret -> {
                    ArrayList<FriendBean> fList = BaseApplication.getAppComponent().getSourceManager().getFriendsList();
                    ArrayList<FriendsReqBean> fReqList = BaseApplication.getAppComponent().getSourceManager().getFriendsReqList();
                    saveToDb(fList, fReqList);
                    TreeHelper helper = BaseApplication.getAppComponent().getTreeHelper();
                    TreeNode node = helper.findTreeNodeByName(MineFriendsFragment.class.getSimpleName());
                    node.setCacheData(new CacheObject().setCount(ListUtils.getSize(fReqList))
                            .setObject(fReqList));
                    RxBus.getCacheInstance().postSticky(new RxEvent.AllFriendsRsp());
                    AppLogger.d("FetchFriendsTask rsp: " + new Gson().toJson(fList) + "h" + helper);
                    AppLogger.d("FetchFriendsTask rsp: " + new Gson().toJson(fReqList));
                    throw new RxEvent.HelperBreaker("yes, job done!");
                }, AppLogger::e);
    }

    private void saveToDb(ArrayList<FriendBean> fList, List<FriendsReqBean> fReqList) {
        AppLogger.d("替换数据库");
        //需要替换数据库
        try {
            BaseDBHelper helper = (BaseDBHelper) BaseApplication.getAppComponent().getDBHelper();
            helper.getDaoSession().getFriendBeanDao().deleteAll();
            helper.getDaoSession().getFriendsReqBeanDao().deleteAll();
            if (fList != null)
                helper.getDaoSession().getFriendBeanDao().saveInTx(fList);
            if (fReqList != null)
                helper.getDaoSession().getFriendsReqBeanDao().saveInTx(fReqList);
        } catch (Exception e) {

        }
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
                .subscribeOn(Schedulers.newThread())
                .flatMap(s -> {
                    BaseApplication.getAppComponent().getCmd().getFriendRequestList();
                    return RxBus.getCacheInstance().toObservable(RxEvent.GetAddReqList.class);
                });
    }
}
