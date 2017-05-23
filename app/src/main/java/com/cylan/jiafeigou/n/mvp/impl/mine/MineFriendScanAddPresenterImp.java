package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendScanAddContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendScanAddPresenterImp extends AbstractPresenter<MineFriendScanAddContract.View>
        implements MineFriendScanAddContract.Presenter {


    private CompositeSubscription compositeSubscription;
    private Network network;
    private boolean isOpenLogin;

    public MineFriendScanAddPresenterImp(MineFriendScanAddContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(beginScan());
            compositeSubscription.add(getUserInfo());
            compositeSubscription.add(checkAccountCallBack());
        }
        registerNetworkMonitor();
    }

    @Override
    public void stop() {
        super.stop();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
        unregisterNetworkMonitor();
    }

    /**
     * 检测扫描结果
     *
     * @param account
     */
    @Override
    public void checkScanAccount(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            BaseApplication.getAppComponent().getCmd().checkFriendAccount(s);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("checkScanAccount" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 扫描结果的回调
     *
     * @return
     */
    @Override
    public Subscription checkAccountCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckAccountCallback>() {
                    @Override
                    public void call(RxEvent.CheckAccountCallback checkAccountCallback) {
                        if (checkAccountCallback != null) {
                            handlerCheckResult(checkAccountCallback);
                        }
                    }
                }, AppLogger::e);
    }

    /**
     * 获取到用户的信息用于产生二维码
     *
     * @return
     */
    @Override
    public Subscription getUserInfo() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getUserInfo -> {
                    if (getUserInfo != null) {
                        if (getView() != null) {
                            getView().showQrCode(getUserInfo.jfgAccount.getAccount());
                        }
                    }
                }, AppLogger::e);
    }

    /**
     * 开始扫描
     *
     * @return
     */
    @Override
    public Subscription beginScan() {
        return Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().onStartScan();
                    }
                }, AppLogger::e);
    }

    /**
     * 处理检测的结果
     *
     * @param checkAccountCallback
     */
    private void handlerCheckResult(RxEvent.CheckAccountCallback checkAccountCallback) {
        if (getView() != null) {
//            getView().hideLoadingPro();
            if (checkAccountCallback.i == 0) {
                // 已注册
                MineAddReqBean resutBean = new MineAddReqBean();
                resutBean.account = checkAccountCallback.s;
                resutBean.alias = checkAccountCallback.s1;
                try {
                    int type = BaseApplication.getAppComponent().getSourceManager().getStorageType();
                    resutBean.iconUrl = BaseApplication.getAppComponent().getCmd().getSignedCloudUrl(type, String.format(Locale.getDefault(), "/image/%s.jpg", checkAccountCallback.s));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getView().jump2FriendDetailFragment(false, resutBean, checkAccountCallback.b);
            } else if (checkAccountCallback.i == 241) {
                // 已经是好友了
                getView().isMineFriendResult();
            } else {
                // 未注册
                getView().scanNoResult();
            }
        }
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

}
