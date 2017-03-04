package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
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
    private long req;

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
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String path) {
                        try {
                            req = JfgCmdInsurance.getCmd().updateAccountPortrait(path);
                            AppLogger.d("upLoadUserHeadImag:"+ req);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("upLoadUserHeadImag: " + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 接收上传头像的回调
     */
    @Override
    public Subscription getUpLoadResult() {
        return RxBus.getCacheInstance().toObservable(JFGMsgHttpResult.class)
                .timeout(30, TimeUnit.SECONDS, Observable.just(null)
                        .observeOn(AndroidSchedulers.mainThread())
                        .map((Object o) -> {
                            Log.d("CYLAN_TAG", "upLoadUserHeadImag timeout: ");
                            if (getView() != null) getView().upLoadTimeOut();
                            return null;
                        }))
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGMsgHttpResult>() {
                    @Override
                    public void call(JFGMsgHttpResult getHttpDoneResult) {
                        getView().hideUpLoadPro();
                        handlerUploadImage(getHttpDoneResult);
                        getView().upLoadResultView(getHttpDoneResult.ret);
                    }
                });
    }

    /**
     * 处理上传头像文件后
     * @param getHttpDoneResult
     */
    private void handlerUploadImage(JFGMsgHttpResult getHttpDoneResult) {
        if (getHttpDoneResult.requestId == req && getHttpDoneResult.ret == 200) {
            sendResetUrl();
        }
    }

    /**
     * 更新头像的Url
     */
    private void sendResetUrl() {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (jfgAccount != null) {
                            try {
                                jfgAccount.resetFlag();
                                jfgAccount.setPhoto(true);
                                int req = JfgCmdInsurance.getCmd().setAccount(jfgAccount);
                                AppLogger.d("sendResetUrl:"+req);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendResetUrl" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到用户的信息
     */
    @Override
    public Subscription getAccount() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null) {
                            jfgAccount = getUserInfo.jfgAccount;
                        }
                    }
                });
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
                });
    }

    @Override
    public void start() {
        super.start();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        } else {
            subscription = new CompositeSubscription();
            subscription.add(getAccount());
            subscription.add(getUpLoadResult());
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
