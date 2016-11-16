package com.cylan.jiafeigou.n.mvp.impl;

import com.cylan.entity.JfgEnum;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.login.ForgetPwdContract;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public class ForgetPwdPresenterImpl extends AbstractPresenter<ForgetPwdContract.View>
        implements ForgetPwdContract.Presenter {

    private Subscription subscription;
    private CompositeSubscription compositeSubscription;

    public ForgetPwdPresenterImpl(ForgetPwdContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void submitAccount(final String account) {
        subscription = Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        final boolean isPhoneNum = JConstant.PHONE_REG.matcher(account).find();
                        if (isPhoneNum) {
                            JfgCmdInsurance.getCmd()
                                    .sendCheckCode(account, JfgEnum.JFG_SMS_FORGOTPASS);
                        } else {
                            JfgCmdInsurance.getCmd().forgetPassByEmail(account);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    @Override
    public void submitPhoneNumAndCode(final String account, final String code) {
        subscription = Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        JfgCmdInsurance.getCmd().verifySMS(account, code, PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
                    }
                });
    }

    @Override
    public void start() {
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(getForgetPwdByMailSub());
        compositeSubscription.add(getSmsCodeResultSub());
    }

    private Subscription getForgetPwdByMailSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ForgetPwdByMail.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ForgetPwdByMail>() {
                    @Override
                    public void call(RxEvent.ForgetPwdByMail forgetPwdByMail) {
                        RequestResetPwdBean bean = new RequestResetPwdBean();
                        bean.ret = JConstant.AUTHORIZE_MAIL;
                        bean.content = forgetPwdByMail.account;
                        getView().submitResult(bean);
                    }
                });
    }

    private Subscription getSmsCodeResultSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SmsCodeResult.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.SmsCodeResult>() {
                    @Override
                    public void call(RxEvent.SmsCodeResult smsCodeResult) {
                        if (smsCodeResult.error == 0) {
                            //store the token .
                            PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN,
                                    smsCodeResult.token);
                        }
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe(subscription, compositeSubscription);
    }
}
