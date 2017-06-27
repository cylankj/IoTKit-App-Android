package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.ex.JfgException;
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


    public MineDevicesShareManagerPresenterImp(MineDevicesShareManagerContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{cancelShareCallBack()};
    }


    @Override
    public void start() {
        super.start();
    }

    @Override
    public void initShareDeviceList(String uuid) {
        Subscription subscribe = Observable.from(DataSourceManager.getInstance().getShareList())
                .subscribeOn(Schedulers.io())
                .first(ret -> TextUtils.equals(uuid, ret.cid))
                .timeout(3, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    getView().onInitShareDeviceList(list.friends);
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

    /**
     * 取消分享设备
     *
     * @param cid
     * @param bean
     */
    @Override
    public void cancelShare(final String cid, final JFGFriendAccount bean) {
        if (getView() != null) {
            getView().showCancleShareProgress();
        }
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    try {
                        AppLogger.e("正在取消分享:" + bean.account);
                        BaseApplication.getAppComponent().getCmd().unShareDevice(cid, bean.account);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, throwable -> AppLogger.e("cancelShare" + throwable.getLocalizedMessage()));
    }

    /**
     * 取消分享的回调
     *
     * @return
     */
    public Subscription cancelShareCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.UnShareDeviceCallBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handlerUnShareCallback, AppLogger::e);
    }

    /**
     * 取消分享回调的处理
     *
     * @param unshareDeviceCallBack
     */
    private void handlerUnShareCallback(RxEvent.UnShareDeviceCallBack unshareDeviceCallBack) {
        if (getView() != null) {
            getView().hideCancleShareProgress();
            getView().showUnShareResult(unshareDeviceCallBack);
        }
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
