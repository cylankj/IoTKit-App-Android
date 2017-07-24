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

/**
 * 作者：zsl
 * 创建时间：2016/11/14
 * 描述：
 */
public class MineBindPhonePresenterImp extends AbstractPresenter<MineBindPhoneContract.View> implements MineBindPhoneContract.Presenter {

    private JFGAccount jfgAccount;

    public MineBindPhonePresenterImp(MineBindPhoneContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void getVerifyCode(final String phone) {
        //获取验证码,1.校验手机号码,2.根据错误号显示
        //保存上次获取验证码的时间,以免退出页面重置.
        addSubscription(Observable.just("getVerifyCode")
                .subscribeOn(AndroidSchedulers.mainThread())
                .delay(200, TimeUnit.MILLISECONDS)//因为太快导致 loading 消失不了,需要 delay 下
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        long req = BaseApplication.getAppComponent().getCmd().checkFriendAccount(phone);
                        Log.d(TAG, "校验手机号码: " + req);
                    } catch (JfgException e) {
                        AppLogger.e(e.getMessage());
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class).first())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(result -> {
                    if (!TextUtils.isEmpty(result.account) && TextUtils.equals(phone, result.account)) {
                        //与当前号码一致.此号码已经被注册
                        //返回错误码
                        mView.onResult(JConstant.CHECK_ACCOUNT, JError.ErrorAccountAlreadyExist);//需要在主线程
                    }
                    return TextUtils.isEmpty(result.account) && TextUtils.equals(mView.getInputPhone(), phone);
                })
                .observeOn(Schedulers.io())
                .map(ret -> {
                    try {
                        //获取验证码
                        BaseApplication.getAppComponent().getCmd().sendCheckCode(phone, JFGRules.getLanguageType(ContextUtils.getContext()), JfgEnum.SMS_TYPE.JFG_SMS_REGISTER);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(e.getMessage());
                    }
                    return ret;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.SmsCodeResult.class).first())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoadingDialog())
                .doOnTerminate(() -> getView().hideLoadingDialog())
                .subscribe(result -> {
                    mView.onResult(JConstant.GET_SMS_BACK, result == null ? -1 : result.error);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                    mView.onResult(JConstant.CHECK_TIMEOUT, 0);
                }));
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
     * 获取到用户信息
     *
     * @return
     */
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
     * 校验短信验证码
     *
     * @param inputCode
     */
    @Override
    public void CheckVerifyCode(String phone, final String inputCode) {
        addSubscription(Observable.just("CheckVerifyCode")
                .subscribeOn(AndroidSchedulers.mainThread())
                .delay(200, TimeUnit.MILLISECONDS)//因为太快导致 loading 消失不了,需要 delay 下
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        String token = PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN, "");
                        BaseApplication.getAppComponent().getCmd().verifySMS(phone, inputCode, token);
                        AppLogger.d("验证 短信:" + token);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.ResultVerifyCode.class).first())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(result -> {
                    if (result.code != JError.ErrorOK) {
                        getView().handlerCheckCodeResult(result);
                    }
                    return result.code == JError.ErrorOK;
                })
                .observeOn(Schedulers.io())
                .map(ret -> {
                    try {
                        String token = PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN, "");
                        jfgAccount.resetFlag();
                        jfgAccount.setPhone(phone, token);
                        int req = BaseApplication.getAppComponent().getCmd().setAccount(jfgAccount);
                        AppLogger.d("sendChangePhoneReq:" + req + ":" + phone + ":" + token);
                    } catch (JfgException e) {
                        AppLogger.d("sendChangePhoneReq:" + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                    return ret;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.RessetAccountBack.class).first())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoadingDialog())
                .doOnTerminate(() -> getView().hideLoadingDialog())
                .subscribe(result -> {
                    getView().handlerResetPhoneResult(result == null ? -1 : result.jfgResult.code);
                }, e -> {
                    AppLogger.e(e.getMessage());
                }));
    }

    @Override
    public void start() {
        super.start();
        addSubscription(getAccountCallBack());
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
