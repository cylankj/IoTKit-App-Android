package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.JfgEnum;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
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

    public MineBindPhonePresenterImp(MineBindPhoneContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void getVerifyCode(final String phone) {
        //获取验证码,1.校验手机号码,2.根据错误号显示
        //保存上次获取验证码的时间,以免退出页面重置.
        Subscription subscription = rx.Observable.just(phone)
                .subscribeOn(Schedulers.newThread())
                .delay(1, TimeUnit.SECONDS)
                .flatMap(s -> {
                    try {
                        long req = BaseApplication.getAppComponent()
                                .getCmd().checkFriendAccount(s);
                        Log.d(TAG, "校验手机号码: " + req);
                        return Observable.just(req);
                    } catch (JfgException e) {
                        return Observable.just(-1);
                    }
                })
                .flatMap(number -> RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class)
                        .first()
                        .subscribeOn(Schedulers.newThread())
                        .timeout(10, TimeUnit.SECONDS)
                        .delay(100, TimeUnit.MILLISECONDS))
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(ret -> {
                    //手机号注册 情况
                    final String inputAccount = mView.getInputPhone();
                    if (TextUtils.isEmpty(ret.account) && TextUtils.equals(inputAccount, phone)) {
                        //此账号不存在.这里不考虑页面频繁更换手机号码
                        //去获取验证码
                        Subscription s = RxBus.getCacheInstance()
                                .toObservable(RxEvent.SmsCodeResult.class)
                                .timeout(10, TimeUnit.SECONDS)
                                .filter(r -> mView != null)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(result -> {
                                    mView.onResult(JConstant.GET_SMS_BACK, result.error);
                                    unSubscribe("ResultVerifyCode");
                                    unSubscribe("getVerifyCode");
                                }, AppLogger::e);
                        addSubscription(s, "ResultVerifyCode");
                        try {
                            //获取验证码
                            int seq = BaseApplication.getAppComponent().getCmd()
                                    .sendCheckCode(phone, JFGRules.getLanguageType(ContextUtils.getContext()),
                                            JfgEnum.SMS_TYPE.JFG_SMS_REGISTER);
                            if (seq != 0) s.unsubscribe();
                            else AppLogger.d("手机号码 有效,开始获取验证码");
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    } else if (!TextUtils.isEmpty(ret.account)
                            && TextUtils.equals(phone, ret.account)) {
                        //与当前号码一致.此号码已经被注册
                        //返回错误码
                        AndroidSchedulers.mainThread().createWorker().schedule(() -> mView.onResult(JConstant.CHECK_ACCOUNT, JError.ErrorAccountAlreadyExist));//需要在主线程
                        unSubscribeAllTag();
                    }
                    throw new RxEvent.HelperBreaker();
                })
                .doOnError(throwable -> {
                    if (throwable instanceof RxEvent.HelperBreaker) {

                    } else {
                        AndroidSchedulers.mainThread().createWorker().schedule(() -> mView.onResult(JConstant.CHECK_TIMEOUT, 0));//需要在主线程
                    }
                })
                .subscribe(ret -> {
                }, AppLogger::e);
        addSubscription(subscription, "getVerifyCode");
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
                .subscribe(s -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().sendCheckCode(phone, JFGRules.getLanguageType(ContextUtils.getContext()), JfgEnum.SMS_TYPE.JFG_SMS_REGISTER);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

//    /**
//     * 检测账号是否已经注册
//     */
//    @Override
//    public void checkPhoneIsBind(String phone) {
//        rx.Observable.just(phone)
//                .subscribeOn(Schedulers.newThread())
//                .subscribe(s -> {
//                    try {
//                        BaseApplication.getAppComponent().getCmd().checkAccountRegState(s);
//                    } catch (JfgException e) {
//                        e.printStackTrace();
//                    }
//                }, AppLogger::e);
//    }

//    /**
//     * 获取到检测账号的回调
//     *
//     * @return
//     */
//    public Subscription getCheckPhoneCallback() {
//        return RxBus.getCacheInstance().toObservable(RxEvent.CheckRegisterBack.class)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(checkAccountCallback -> {
//                    if (getView() != null) {
//                        getView().handlerCheckPhoneResult(checkAccountCallback);
//                    }
//                }, e -> AppLogger.d("getCheckPhoneCallback" + e.getMessage()));
//    }

    /**
     * 发送修改phone的请求
     */
    @Override
    public void sendChangePhoneReq(String newPhone, String token) {
        rx.Observable.just(jfgAccount)
                .subscribeOn(Schedulers.newThread())
                .subscribe(account -> {
                    try {
                        account.resetFlag();
                        account.setPhone(newPhone, token);
                        int req = BaseApplication.getAppComponent().getCmd().setAccount(account);
                        AppLogger.d("sendChangePhoneReq:" + req + ":" + newPhone + ":" + token);
                    } catch (JfgException e) {
                        AppLogger.d("sendChangePhoneReq:" + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    /**
     * 获取到用户信息
     *
     * @return
     */
    @Override
    public Subscription getAccountCallBack() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getUserInfo -> {
                    if (getUserInfo != null) {
                        jfgAccount = getUserInfo.jfgAccount;
                    }
                }, e -> AppLogger.d(e.getMessage()));
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
                .subscribe(resultVerifyCode -> {
                    if (getView() != null) {
                        getView().handlerCheckCodeResult(resultVerifyCode);
                    }
                }, e -> AppLogger.d(e.getMessage()));
    }

    /**
     * 校验短信验证码
     *
     * @param inputCode
     */
    @Override
    public void CheckVerifyCode(String phone, final String inputCode) {
        unSubscribeAllTag();
        Observable.just(phone)
                .subscribeOn(Schedulers.io())
                .flatMap(s -> {
                    Subscription codeResultSub = RxBus.getCacheInstance().toObservable(RxEvent.ResultVerifyCode.class)
                            .first()
                            .timeout(10, TimeUnit.SECONDS)
                            .filter(ret -> mView != null)
                            .doOnError(throwable -> mView.onResult(JConstant.CHECK_TIMEOUT, 0))
                            .subscribe(ret -> {
                                mView.onResult(JConstant.AUTHORIZE_PHONE_SMS, ret.result.code);
                                throw new RxEvent.HelperBreaker("");
                            }, AppLogger::e);
                    addSubscription(codeResultSub, "codeResultSub");
                    try {
                        String token = PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN, "");
                        BaseApplication.getAppComponent().getCmd().verifySMS(phone, inputCode, token);
                        AppLogger.d("验证 短信:" + token);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .timeout(10, TimeUnit.SECONDS)
                .subscribe(ret -> {
                }, AppLogger::e);
    }

    @Override
    public void start() {
        super.start();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getAccountCallBack());
            compositeSubscription.add(changeAccountBack());
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
        updateConnectivityStatus(status.state);
    }

    /**
     * 是否三方登录
     *
     * @return
     */
    @Override
    public boolean isOpenLogin() {
        return BaseApplication.getAppComponent().getSourceManager().getAccount().getLoginType() >= 3;
    }

    @Override
    public Subscription changeAccountBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.RessetAccountBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp != null) {
                        getView().handlerResetPhoneResult(rsp.jfgResult.code);
                    }
                }, e -> AppLogger.d(e.getMessage()));
    }

    /**
     * 连接状态变化
     */
    private void updateConnectivityStatus(int network) {
        Observable.just(network)
                .filter(integer -> getView() != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> getView().onNetStateChanged(integer), e -> AppLogger.d(e.getMessage()));
    }
}
