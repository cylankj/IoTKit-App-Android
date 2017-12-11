package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendInformationContact;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineFriendInformationPresenter extends AbstractPresenter<MineFriendInformationContact.View> implements MineFriendInformationContact.Presenter {

    private Network network;

    public MineFriendInformationPresenter(MineFriendInformationContact.View view) {
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
    public void deleteFriend(FriendContextItem friendContextItem) {
        Subscription subscription = Observable.just("deleteFriend")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        Command.getInstance().delFriend(friendContextItem.friendAccount.account);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.DelFriendBack.class).first())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading(R.string.DELETEING))
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(result -> {
                    getView().onDeleteResult(result == null ? -1 : result.jfgResult.code);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscription);
    }

    @Override
    public void consentFriend(FriendContextItem friendContextItem) {
        Subscription subscribe = Observable.just("consentFriend")
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(cmd -> {
                    //是接受添加请求还是主动添加请求,如果是主动添加请求则跳转到设置 SayHi 页面,接受添加请求才继续往下走
                    if (NetUtils.getNetType(ContextUtils.getContext()) == -1) {//无网络连接
                        getView().onNetStateChanged(0);
                        return false;
                    } else if (TextUtils.isEmpty(friendContextItem.friendRequest.sayHi)) {
                        //主动添加请求,不继续走下面的逻辑了
                        getView().onRequestByOwner(friendContextItem);
                        return false;
                    } else if (!checkRequestAvailable(friendContextItem)) {
                        getView().onRequestExpired(friendContextItem);
                        return false;
                    } else {
                        return true;
                    }
                })
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        Command.getInstance().consentAddFriend(friendContextItem.friendRequest.account);
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
                    getView().acceptItemRsp(friendContextItem, ret == null ? -1 : ret.jfgResult.code);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);

    }

    public boolean checkRequestAvailable(FriendContextItem bean) {
        long oneMonth = 30 * 24 * 60 * 60 * 1000L;
        long current = System.currentTimeMillis();
        boolean isLongTime = String.valueOf(bean.friendRequest.time).length() == String.valueOf(current).length();
        return (current - (isLongTime ? bean.friendRequest.time : bean.friendRequest.time * 1000L)) < oneMonth;
    }


    @Override
    public int getOwnerDeviceCount() {
        List<Device> devices = DataSourceManager.getInstance().getAllDevice();
        int ownerDeviceCount = 0;
        if (devices != null) {
            for (Device device : devices) {
                if (TextUtils.isEmpty(device.shareAccount)) {
                    ownerDeviceCount++;
                }
            }
        }
        return ownerDeviceCount;
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
