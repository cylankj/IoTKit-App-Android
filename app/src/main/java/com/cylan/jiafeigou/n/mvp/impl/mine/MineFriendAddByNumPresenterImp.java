package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendSearchContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineFriendAddByNumPresenterImp extends AbstractPresenter<MineFriendSearchContract.View>
        implements MineFriendSearchContract.Presenter {

    private Network network;

    public MineFriendAddByNumPresenterImp(MineFriendSearchContract.View view) {
        super(view);
    }

    @Override
    public void start() {
        super.start();
        registerNetworkMonitor();
    }

    @Override
    public void stop() {
        super.stop();
        unregisterNetworkMonitor();
    }

    /**
     * 检测好友账号是否注册过
     */
    @Override
    public void checkFriendAccount(final String account) {
        Subscription subscribe = Observable.just("checkFriendAccount")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        Command.getInstance().checkFriendAccount(account);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class).first())
                .map(result -> {
                    FriendContextItem friendContextItem = null;
                    if (result != null && result.code == JError.ErrorOK) {
                        if (result.isFriend) {
                            JFGFriendAccount friendAccount = null;
                            ArrayList<JFGFriendAccount> friendsList = DataSourceManager.getInstance().getFriendsList();
                            if (friendsList != null) {
                                for (JFGFriendAccount friend : friendsList) {
                                    if (TextUtils.equals(friend.account, result.account)) {
                                        friendAccount = friend;
                                        break;
                                    }
                                }
                            }
                            if (friendAccount == null) {
                                friendAccount = new JFGFriendAccount(result.account, null, result.alias);
                            }
                            friendContextItem = new FriendContextItem(friendAccount);
                        } else {
                            JFGFriendRequest friendRequest = null;

                            ArrayList<JFGFriendRequest> friendsReqList = DataSourceManager.getInstance().getFriendsReqList();
                            if (friendsReqList != null) {//这里我们判断当前是否有该好友的添加请求
                                for (JFGFriendRequest request : friendsReqList) {
                                    if (TextUtils.equals(request.account, result.account)) {
                                        friendRequest = request;
                                        break;
                                    }
                                }
                            }

                            if (friendRequest == null) {
                                friendRequest = new JFGFriendRequest();
                                friendRequest.time = System.currentTimeMillis();
                                friendRequest.account = result.account;
                                friendRequest.alias = result.alias;
                                friendContextItem = new FriendContextItem(friendRequest);
                            }
                        }
                    }
                    return friendContextItem;
                })
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading(R.string.LOADING))
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(friendContextItem -> {
                    getView().onCheckFriendResult(friendContextItem);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

    public void registerNetworkMonitor() {
        try {
            if (network == null) {
                network = new Network();
                final IntentFilter filter = new IntentFilter();
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                ContextUtils.getContext().registerReceiver(network, filter);
            }
        } catch (Exception e) {
            AppLogger.e("registerNetworkMonitor" + e.getLocalizedMessage());
        }
    }

    public void unregisterNetworkMonitor() {
        if (network != null) {
            ContextUtils.getContext().unregisterReceiver(network);
            network = null;
        }
    }

    /**
     * 监听网络状态
     */
    private class Network extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
                updateConnectivityStatus(status.state);
            }
        }
    }

    /**
     * 连接状态变化
     */
    private void updateConnectivityStatus(int network) {
        Observable.just(network)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return getView() != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        getView().onNetStateChanged(integer);
                    }
                }, AppLogger::e);
    }

}
