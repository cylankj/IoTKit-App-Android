package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.JfgEnum;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineBindPhoneContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

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
 * 创建时间：2016/11/14
 * 描述：
 */
public class MineBindPhonePresenterImp extends AbstractPresenter<MineBindPhoneContract.View> implements MineBindPhoneContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private JFGAccount jfgAccount;
    private Network network;
    private boolean isOpenLogin;
    private boolean sendReq;

    public MineBindPhonePresenterImp(MineBindPhoneContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void isBindOrChange(JFGAccount userinfo) {
        if (getView() != null && userinfo != null) {
            if (TextUtils.isEmpty(userinfo.getPhone())) {
                //绑定手机号
                getView().initToolbarTitle(getView().getContext().getString(R.string.Tap0_BindPhoneNo));
            } else {
                //修改手机号
                getView().initToolbarTitle(getView().getContext().getString(R.string.CHANGE_PHONE_NUM));
            }
        }
    }

    /**
     * 获取到验证码
     *
     * @param phone
     */
    @Override
    public void getCheckCode(final String phone) {
        rx.Observable.just(phone)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        JfgCmdInsurance.getCmd().sendCheckCode(phone, JFGRules.getLanguageType(ContextUtils.getContext()), JfgEnum.SMS_TYPE.JFG_SMS_REGISTER);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getcheckcode" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 检测账号是否已经注册
     */
    @Override
    public void checkPhoneIsBind(String phone) {
        rx.Observable.just(phone)
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
                        AppLogger.e("checkphoneisbind" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到检测账号的回调
     *
     * @return
     */
    @Override
    public Subscription getCheckPhoneCallback() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckAccountCallback>() {
                    @Override
                    public void call(RxEvent.CheckAccountCallback checkAccountCallback) {
                        if (checkAccountCallback != null) {
                            if (getView() != null) {
                                getView().handlerCheckPhoneResult(checkAccountCallback);
                            }
                        }
                    }
                });
    }

    /**
     * 发送修改phone的请求
     */
    @Override
    public void sendChangePhoneReq(String newPhone, String token) {
        rx.Observable.just(jfgAccount)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<JFGAccount>() {
                    @Override
                    public void call(JFGAccount account) {
                        try {
                            account.resetFlag();
                            account.setPhone(newPhone, token);
                            int req = JfgCmdInsurance.getCmd().setAccount(account);
                            sendReq = true;
                            AppLogger.d("sendChangePhoneReq:" + req + ":" + newPhone + ":" + token);
                        } catch (JfgException e) {
                            AppLogger.d("sendChangePhoneReq:" + e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendChangePhoneReq" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到验证码的回调
     *
     * @return
     */
    @Override
    public Subscription getCheckCodeCallback() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SmsCodeResult.class)
                .delay(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.SmsCodeResult>() {
                    @Override
                    public void call(RxEvent.SmsCodeResult smsCodeResult) {
                        if (smsCodeResult != null) {
                            if (smsCodeResult.error == JError.ErrorOK) {
                                AppLogger.d("getCheckCodeCallback" + smsCodeResult.token);
                                PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, smsCodeResult.token);
                            } else {
                                getView().getSmsCodeResult(smsCodeResult.error);
                            }
                        }
                    }
                });
    }

    /**
     * 获取到用户信息
     *
     * @return
     */
    @Override
    public Subscription getAccountCallBack() {
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

    /**
     * 短信校验的结果回调
     *
     * @return
     */
    @Override
    public Subscription checkVerifyCodeCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultVerifyCode.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ResultVerifyCode>() {
                    @Override
                    public void call(RxEvent.ResultVerifyCode resultVerifyCode) {
                        if (resultVerifyCode != null) {
                            if (getView() != null) {
                                getView().handlerCheckCodeResult(resultVerifyCode);
                            }
                        }
                    }
                });
    }

    /**
     * 校验短信验证码
     *
     * @param code
     */
    @Override
    public void CheckVerifyCode(String phone, final String inputCode, String code) {
        rx.Observable.just(code)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String code) {
                        try {
                            JfgCmdInsurance.getCmd().verifySMS(phone, inputCode, code);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("CheckVerifyCode" + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(openLoginBack());
            compositeSubscription.add(getAccountCallBack());
            compositeSubscription.add(getCheckPhoneCallback());
            compositeSubscription.add(getCheckCodeCallback());
            compositeSubscription.add(checkVerifyCodeCallBack());
            compositeSubscription.add(changeAccountBack());
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
     * 三方登录的回调
     *
     * @return
     */
    @Override
    public Subscription openLoginBack() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.ThirdLoginTab.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(thirdLoginTab -> {
                    isOpenLogin = thirdLoginTab.isThird;
                });
    }

    /**
     * 是否三方登录
     *
     * @return
     */
    @Override
    public boolean isOpenLogin() {
        return isOpenLogin;
    }

    @Override
    public Subscription changeAccountBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.RessetAccountBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp->{
                    if (rsp != null){
                        if (sendReq) {
                            getView().handlerResetPhoneResult(rsp.jfgResult.code);
                            sendReq = false;
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
