package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.n.view.mine.MineFriendsFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.CacheObject;
import com.cylan.jiafeigou.support.badge.TreeHelper;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.NetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendsPresenterImp extends AbstractPresenter<MineFriendsContract.View> implements MineFriendsContract.Presenter {

    public MineFriendsPresenterImp(MineFriendsContract.View view) {
        super(view);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }


    @Override
    public boolean checkRequestAvailable(FriendContextItem bean) {
        long oneMonth = 30 * 24 * 60 * 60 * 1000L;
        long current = System.currentTimeMillis();
        boolean isLongTime = String.valueOf(bean.friendRequest.time).length() == String.valueOf(current).length();
        return (current - (isLongTime ? bean.friendRequest.time : bean.friendRequest.time * 1000L)) < oneMonth;
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{
                ConnectivityManager.CONNECTIVITY_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION
        };
    }

    @Override
    public void initRequestAndFriendList() {
        Subscription subscribe = Observable.just("initRequestAndFriendList")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap(cmd -> {
                    Command.getInstance().getFriendRequestList();
                    return RxBus.getCacheInstance().toObservable(RxEvent.GetAddReqList.class).first();
                })
                .flatMap(ret -> {
                    Command.getInstance().getFriendList();
                    return RxBus.getCacheInstance().toObservable(RxEvent.GetFriendList.class).first();
                })
                .map(ret -> {
                    JFGSourceManager manager = DataSourceManager.getInstance();
                    ArrayList<JFGFriendRequest> friendsReqList = manager.getFriendsReqList();
                    ArrayList<JFGFriendAccount> friendsList = manager.getFriendsList();
                    FriendContextItem contextItem;
                    List<FriendContextItem> requestItems = null;
                    List<FriendContextItem> friendItems = null;
                    if (friendsReqList != null && friendsReqList.size() > 0) {
                        requestItems = new ArrayList<>(friendsReqList.size());
                        for (int i = 0; i < friendsReqList.size(); i++) {
                            contextItem = new FriendContextItem(friendsReqList.get(i));
                            requestItems.add(contextItem);
                        }
                    }
                    if (friendsList != null && friendsList.size() > 0) {
                        friendItems = new ArrayList<>(friendsList.size());
                        for (int i = 0; i < friendsList.size(); i++) {
                            contextItem = new FriendContextItem(friendsList.get(i));
                            friendItems.add(contextItem);
                        }
                    }
                    return new Pair<>(requestItems, friendItems);
                })
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading(R.string.LOADING))
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(result -> {
                    getView().onInitRequestAndFriendList(result == null ? null : result.first, result == null ? null : result.second);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

    @Override
    public void deleteFriendRequest(FriendContextItem item, boolean alert) {
        Subscription subscribe = Observable.just("deleteFriendRequest")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        Command.getInstance().delAddFriendMsg(item.friendRequest.account);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(e.getMessage());
                    }
                    return cmd;
                })
                .flatMap(o -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteAddReqBack.class)
                        .first(ret -> {
                            if (ret.jfgResult.code == JError.ErrorOK) {
                                // TODO: 2017/6/29 删除数据库中的数据
                                AppLogger.d("需要更新缓存");
                                ArrayList<JFGFriendAccount> friendsList = DataSourceManager.getInstance().getFriendsList();
                                friendsList.remove(item.friendAccount);
                                ArrayList<JFGFriendRequest> friendsReqList = DataSourceManager.getInstance().getFriendsReqList();
                                TreeHelper helper = BaseApplication.getAppComponent().getTreeHelper();
                                TreeNode node = helper.findTreeNodeByName(MineFriendsFragment.class.getSimpleName());
                                node.setCacheData(new CacheObject().setCount(friendsReqList == null ? 0 : ListUtils.getSize(friendsReqList)).setObject(friendsList));
                            }
                            return true;
                        }))

                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    getView().deleteItemRsp(item, ret == null ? -1 : ret.jfgResult.code, alert);
                }, AppLogger::e);

        addSubscription(subscribe);
    }

    @Override
    public void acceptFriendRequest(FriendContextItem item) {
        Subscription subscribe = Observable.just("acceptFriendRequest")
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(cmd -> {
                    boolean pass;
                    if (NetUtils.getNetType(ContextUtils.getContext()) == -1) {//无网络连接
                        pass = false;
                        getView().onNetStateChanged(0);
                    } else if (!(pass = checkRequestAvailable(item))) {
                        getView().onRequestExpired(item, true);
                    }
                    return pass;
                })
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        Command.getInstance().consentAddFriend(item.friendRequest.account);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(e.getMessage());
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.ConsentAddFriendBack.class).first())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading(R.string.LOADING))
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(ret -> {
                    getView().acceptItemRsp(item, ret == null ? -1 : ret.jfgResult.code);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }


    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        if (mView == null || !mView.isAdded()) {
            return;
        }
        Observable.just(NetUtils.getJfgNetType())
                .filter(integer -> getView() != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> getView().onNetStateChanged(integer), AppLogger::e);
    }


}
