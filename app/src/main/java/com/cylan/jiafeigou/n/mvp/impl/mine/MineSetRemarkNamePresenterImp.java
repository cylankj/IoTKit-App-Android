package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineSetRemarkNameContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
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
 * 创建时间：2016/9/23
 * 描述：
 */
public class MineSetRemarkNamePresenterImp extends AbstractPresenter<MineSetRemarkNameContract.View> implements MineSetRemarkNameContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private Network network;

    public MineSetRemarkNamePresenterImp(MineSetRemarkNameContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getFriendRemarkNameCallBack());
        }
        registerNetworkMonitor();
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
        unregisterNetworkMonitor();
    }

    @Override
    public boolean isEditEmpty(String string) {
        return TextUtils.isEmpty(string) ? true : false;
    }

    /**
     * 发送修改备注名的请求
     *
     * @param friendBean
     */
    @Override
    public void sendSetmarkNameReq(final String newName, final RelAndFriendBean friendBean) {
        getView().showSendReqPro();
        rx.Observable.just(friendBean)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<RelAndFriendBean>() {
                    @Override
                    public void call(RelAndFriendBean bean) {
                        try {
                            JfgCmdInsurance.getCmd().setFriendMarkName(friendBean.account, newName);
                            SystemClock.sleep(500);
                            JfgCmdInsurance.getCmd().getFriendInfo(friendBean.account);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendSetmarkNameReq: " + throwable.getLocalizedMessage());
                    }
                });

    }

    /**
     * 设置好友的备注名回调
     *
     * @return
     */
    @Override
    public Subscription getFriendRemarkNameCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SetFriendMarkNameBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.SetFriendMarkNameBack rsp)->{
                    if (rsp != null) {
                        if (getView() != null) {
                            getView().hideSendReqPro();
                            getView().showFinishResult(rsp);
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


}
