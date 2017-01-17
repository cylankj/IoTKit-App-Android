package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.JfgEnum;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddByNumContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.utils.PackageUtils;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineFriendAddByNumPresenterImp extends AbstractPresenter<MineFriendAddByNumContract.View>
        implements MineFriendAddByNumContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private Network network;

    public MineFriendAddByNumPresenterImp(MineFriendAddByNumContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            unSubscribe(compositeSubscription);
        }
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(checkFriendAccountCallBack());
        registerNetworkMonitor();
    }

    @Override
    public void stop() {
        super.stop();
        unSubscribe(compositeSubscription);
        unregisterNetworkMonitor();
    }


    /**
     * 是否想我发送过请求
     *
     * @param bean
     */
    @Override
    public void checkIsSendAddReqToMe(MineAddReqBean bean) {

    }

    /**
     * 检测好友账号是否注册过
     */
    @Override
    public void checkFriendAccount(final String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String account) {
                        try {
                            JfgCmdInsurance.getCmd().checkFriendAccount(account);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.d("checkFriendAccount" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 检测好友的回调
     *
     * @return
     */
    @Override
    public Subscription checkFriendAccountCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckAccountCallback>() {
                    @Override
                    public void call(RxEvent.CheckAccountCallback checkAccountCallback) {
                        if (checkAccountCallback != null) {
                            handlerCheckCallBackResult(checkAccountCallback);
                        }
                    }
                });
    }

    /**
     * 处理检测的回调结果
     *
     * @param checkAccountCallback
     */
    private void handlerCheckCallBackResult(RxEvent.CheckAccountCallback checkAccountCallback) {
        if (checkAccountCallback.i == JError.ErrorOK) {
            //  已注册
            if (getView() != null) {
                MineAddReqBean addReqBean = new MineAddReqBean();
                addReqBean.account = checkAccountCallback.s;
                addReqBean.alias = checkAccountCallback.s1;
                try {
                    addReqBean.iconUrl = JfgCmdInsurance.getCmd().getCloudUrlByType(JfgEnum.JFG_URL.PORTRAIT, 0, checkAccountCallback.s + ".jpg", ""
                            , PackageUtils.getMetaString(ContextUtils.getContext(), "vid"));
                } catch (JfgException e) {
                    e.printStackTrace();
                }
                getView().hideFindLoading();
                getView().setFindResult(false, addReqBean);
            }
        } else {
            //  未注册 无结果
            if (getView() != null) {
                getView().hideFindLoading();
                getView().showFindNoResult();
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
                });
    }

}
