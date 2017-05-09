package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetAliasContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ContextUtils;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public class MineInfoSetNamePresenterImpl extends AbstractPresenter<MineInfoSetAliasContract.View> implements MineInfoSetAliasContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private JFGAccount jfgAccount;
    private Network network;
    private boolean isSetAlais;

    public MineInfoSetNamePresenterImpl(MineInfoSetAliasContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    /**
     * 发送保存昵称的请求
     */
    @Override
    public void saveName(final String newAlias) {
        if (getView() != null) {
            getView().showSendHint();
        }
        rx.Observable.just(newAlias)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String newAliasAccount) {
                        jfgAccount.resetFlag();
                        jfgAccount.setAlias(newAliasAccount);
                        try {
                            BaseApplication.getAppComponent().getCmd().setAccount(jfgAccount);
                            isSetAlais = true;
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("saveName" + throwable.getLocalizedMessage());
                    }
                });

    }

    @Override
    public boolean isEditEmpty(String string) {
        return TextUtils.isEmpty(string);
    }

    /**
     * 修改昵称之后的回调
     *
     * @return
     */
    @Override
    public Subscription saveAliasCallBack() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountArrived -> {
                    if (getView() != null) {
                        jfgAccount = accountArrived.jfgAccount;
                        if (isSetAlais) {
                            getView().handlerResult(accountArrived);
                            isSetAlais = false;
                        }
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
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(saveAliasCallBack());
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
}
