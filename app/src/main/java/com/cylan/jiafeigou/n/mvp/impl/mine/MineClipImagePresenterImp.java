package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineClipImageContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/11/7
 * 描述：
 */
public class MineClipImagePresenterImp extends AbstractPresenter<MineClipImageContract.View> implements MineClipImageContract.Presenter {

    private CompositeSubscription subscription;
    public JFGAccount jfgAccount;
    private Network network;

    public MineClipImagePresenterImp(MineClipImageContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    /**
     * 上传用户的头像
     *
     * @param path
     */
    @Override
    public void upLoadUserHeadImag(String path) {
        rx.Observable.just(path)
                .subscribeOn(Schedulers.io())
                .map(path1 -> {
                    long req = -1;
                    try {
                        req = BaseApplication.getAppComponent().getCmd().updateAccountPortrait(path1);
                        AppLogger.d("upLoadUserHeadImag:" + req + ",:" + path1);
                    } catch (JfgException e) {
                        AppLogger.e(e.getMessage());
                    }
                    return req;
                })
                .filter(req -> req != -1)
                .flatMap(req -> RxBus.getCacheInstance().toObservable(JFGMsgHttpResult.class).filter(rsp -> rsp.requestId == req))
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(result -> {
                    if (result != null && result.ret == 200) {
                        getView().hideUpLoadPro();
                        getView().upLoadResultView(result.ret);
                        return true;
                    } else if (result == null) {
                        getView().upLoadTimeOut();
                    }
                    return false;
                })
                .observeOn(Schedulers.io())
                .subscribe(result -> {
                    if (jfgAccount != null) {
                        try {
                            jfgAccount.resetFlag();
                            jfgAccount.setPhoto(true);
                            int req = BaseApplication.getAppComponent().getCmd().setAccount(jfgAccount);
                            AppLogger.d("sendResetUrl:" + req);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
    }

    /**
     * 获取到用户的信息
     */
    @Override
    public Subscription getAccount() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getUserInfo -> {
                    if (getUserInfo != null) {
                        jfgAccount = getUserInfo.jfgAccount;
                    }
                }, AppLogger::e);
    }

    @Override
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

    @Override
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

    @Override
    public void start() {
        super.start();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        } else {
            subscription = new CompositeSubscription();
            subscription.add(getAccount());
        }
        registerNetworkMonitor();
    }

    @Override
    public void stop() {
        super.stop();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        unregisterNetworkMonitor();
    }
}
