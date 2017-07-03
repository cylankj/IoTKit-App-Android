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
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendScanAddContract;
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
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendScanAddPresenterImp extends AbstractPresenter<MineFriendScanAddContract.View>
        implements MineFriendScanAddContract.Presenter {

    private Network network;

    public MineFriendScanAddPresenterImp(MineFriendScanAddContract.View view) {
        super(view);
        view.setPresenter(this);
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

    @Override
    public void checkFriendAccount(String account) {
        Subscription subscribe = Observable.just("checkFriendAccount")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().checkFriendAccount(account);
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
                            ArrayList<JFGFriendAccount> friendsList = BaseApplication.getAppComponent().getSourceManager().getFriendsList();
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
                            JFGFriendRequest request = new JFGFriendRequest();
                            friendContextItem = new FriendContextItem(request);
                        }
                    }
                    return friendContextItem;
                })
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(friendContextItem -> {
                    getView().onCheckFriendAccountResult(friendContextItem);
                }, e -> {
                    if (e instanceof TimeoutException) {
                        // TODO: 2017/7/1 超时了
                    }
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
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
                getView().onNetStateChanged(status.state);
            }
        }
    }

}
