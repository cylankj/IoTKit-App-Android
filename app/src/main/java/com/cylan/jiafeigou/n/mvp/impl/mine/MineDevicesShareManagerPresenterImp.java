package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineDevicesShareManagerContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/8
 * 描述：
 */
public class MineDevicesShareManagerPresenterImp extends AbstractPresenter<MineDevicesShareManagerContract.View>
        implements MineDevicesShareManagerContract.Presenter {
    private JFGShareListInfo shareListInfo;

    public MineDevicesShareManagerPresenterImp(MineDevicesShareManagerContract.View view) {
        super(view);
    }


    @Override
    public void start() {
        super.start();
    }

    @Override
    public void initShareDeviceList(String uuid) {
        Subscription subscribe = Observable.just(DataSourceManager.getInstance().getShareListByCid(uuid))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    getView().onInitShareDeviceList((shareListInfo = list).friends);
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

    /**
     * 取消分享设备
     */
    @Override
    public void cancelShare(int position) {
        Subscription subscribe = Observable.just("正在取消分享")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(ret -> {
                    int seq = 0;
                    try {
                        JFGFriendAccount account = shareListInfo.friends.get(position);
                        AppLogger.e("正在取消分享:" + account.account);
                        seq = BaseApplication.getAppComponent().getCmd().unShareDevice(uuid, account.account);
                    } catch (Exception e) {
                        e.printStackTrace();
                        AppLogger.e(e.getMessage());
                    }
                    return seq;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.UnShareDeviceCallBack.class).first())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showCancelShareProgress())
                .doOnTerminate(() -> getView().hideCancelShareProgress())
                .subscribe(result -> {
                    if (result != null && result.i == 0 && shareListInfo != null && position < shareListInfo.friends.size()) {
                        shareListInfo.friends.remove(position);
                    }
                    getView().showUnShareResult(position, result);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        final String action = intent.getAction();
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
            updateConnectivityStatus(status.state);
        }
    }

    /**
     * 连接状态变化
     */
    private void updateConnectivityStatus(int network) {
        Observable.just(network)
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    getView().onNetStateChanged(integer);
                }, AppLogger::e);
    }

}
