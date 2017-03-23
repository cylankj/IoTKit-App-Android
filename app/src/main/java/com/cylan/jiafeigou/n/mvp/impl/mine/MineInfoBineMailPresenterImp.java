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
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoBindMailContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/12
 * 描述：
 */
public class MineInfoBineMailPresenterImp extends AbstractPresenter<MineInfoBindMailContract.View> implements MineInfoBindMailContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private JFGAccount jfgAccount;
    private Network network;
    private boolean isOpenLogin;

    public MineInfoBineMailPresenterImp(MineInfoBindMailContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public boolean checkEmail(String email) {
        return JConstant.EMAIL_REG.matcher(email).find();
    }

    @Override
    public void checkEmailIsBinded(final String email) {
        rx.Observable.just(email)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            JfgCmdInsurance.getCmd().checkFriendAccount(email);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                },throwable -> {
                    AppLogger.d("checkEmailIsBinded" + throwable.getLocalizedMessage());
                });
    }

    @Override
    public boolean checkAccoutIsPhone(String account) {
        String telRegex = "[1][358]\\d{9}";
        if (account.matches(telRegex)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 发送修改用户属性请求
     */
    @Override
    public void sendSetAccountReq(String newEmail) {
        rx.Observable.just(newEmail)
                .delay(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String newEmail) {
                        jfgAccount.resetFlag();
                        jfgAccount.setEmail(newEmail);
                        try {
                            JfgCmdInsurance.getCmd().setAccount(jfgAccount);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendSetAccountReq" + throwable.getLocalizedMessage());
                    }
                });

        getView().jump2MailConnectFragment();
    }

    /**
     * 检验邮箱是否已经注册过
     *
     * @return
     */
    @Override
    public Subscription getCheckAccountCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckAccountCallback>() {
                    @Override
                    public void call(RxEvent.CheckAccountCallback checkAccountCallback) {
                        if (checkAccountCallback != null) {
                            handlerCheckAccoutResult(checkAccountCallback);
                        }
                    }
                });
    }

    /**
     * 修改属性后的回调
     */
    @Override
    public Subscription getChangeAccountCallBack() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null && getUserInfo instanceof RxEvent.GetUserInfo) {
                            jfgAccount = getUserInfo.jfgAccount;
                            getView().getUserAccountData(getUserInfo.jfgAccount);
//                            getView().showSendReqResult(getUserInfo);
                        }
                    }
                });

    }

    /**
     * 拿到用户的账号
     *
     * @return
     */
    @Override
    public JFGAccount getUserAccount() {
        return jfgAccount;
    }


    /**
     * 处理检测邮箱是否绑定后结果
     */
    private void handlerCheckAccoutResult(RxEvent.CheckAccountCallback checkAccountCallback) {
        if (checkAccountCallback.i == 0) {
            //已经注册过
            if (getView() != null) {
                getView().showMailHasBindDialog();
            }
        } else {
            // 没有注册过 查不到账号回调为空
            if (getView() != null) {
                getView().showAccountUnReg();
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
     * 是否上方登录
     *
     * @return
     */
    @Override
    public Subscription isOpenLoginBack() {
        return RxBus.getCacheInstance().toObservableSticky(Boolean.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        isOpenLogin = aBoolean;
                    }
                });
    }

    @Override
    public boolean isOpenLogin() {
        return isOpenLogin;
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
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(isOpenLoginBack());
            compositeSubscription.add(getChangeAccountCallBack());
            compositeSubscription.add(getCheckAccountCallBack());
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
}
