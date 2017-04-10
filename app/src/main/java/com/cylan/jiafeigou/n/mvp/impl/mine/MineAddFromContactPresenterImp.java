package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineAddFromContactContract;
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
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineAddFromContactPresenterImp extends AbstractPresenter<MineAddFromContactContract.View> implements MineAddFromContactContract.Presenter {

    private String userAlids = "";
    private CompositeSubscription compositeSubscription;
    private Network network;
    private boolean isSendReq;

    public MineAddFromContactPresenterImp(MineAddFromContactContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getAccountAlids());
            compositeSubscription.add(checkAccountCallBack());
            compositeSubscription.add(sendAddFriendRep());
        }
        registerNetworkMonitor();
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
        unregisterNetworkMonitor();
    }

    @Override
    public void sendRequest(final String account, final String mesg) {
        rx.Observable.just(account, mesg)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            JfgCmdInsurance.getCmd().addFriend(account, mesg);
                            isSendReq = true;
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendRequest" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到账号昵称
     *
     * @return
     */
    @Override
    public Subscription getAccountAlids() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null) {
                            if (getView() != null)
                                getView().initEditText(getUserInfo.jfgAccount.getAlias());
                            userAlids = getUserInfo.jfgAccount.getAlias();
                        }
                    }
                });
    }

    /**
     * 获取到用户的昵称
     *
     * @return
     */
    @Override
    public String getUserAlias() {
        return userAlids;
    }

    /**
     * 检测账号
     *
     * @param account
     */
    @Override
    public void checkAccount(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            JfgCmdInsurance.getCmd().checkFriendAccount(s);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("checkoutAccount" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 检测账号的回调
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
                            if (getView() != null) {
                                getView().showResultDialog(checkAccountCallback);
                            }
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
     * 发送添加请求的回调
     *
     * @return
     */
    @Override
    public Subscription sendAddFriendRep() {
        return RxBus.getCacheInstance().toObservable(RxEvent.AddFriendBack.class)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.AddFriendBack>() {
                    @Override
                    public void call(RxEvent.AddFriendBack addFriendBack) {
                        if (addFriendBack != null && isSendReq) {
                            getView().sendReqBack(addFriendBack.jfgResult.code);
                            isSendReq = false;
                        }
                    }
                });
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
