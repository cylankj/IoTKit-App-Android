package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.JfgEnum;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineBindPhoneContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/11/14
 * 描述：
 */
public class MineBindPhonePresenterImp extends AbstractPresenter<MineBindPhoneContract.View> implements MineBindPhoneContract.Presenter{

    private CompositeSubscription compositeSubscription;

    public MineBindPhonePresenterImp(MineBindPhoneContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void isBindOrChange(JFGAccount userinfo) {
        if (getView() != null && userinfo != null){
            if ("".equals(userinfo.getPhone()) || userinfo.getPhone() == null){
                //绑定手机号
                getView().initToolbarTitle(getView().getContext().getString(R.string.Tap0_BindPhoneNo));
            }else {
                //修改手机号
                getView().initToolbarTitle(getView().getContext().getString(R.string.CHANGE_PHONE_NUM));
            }
        }
    }

    /**
     * 获取到验证码
     * @param phone
     */
    @Override
    public void getCheckCode(final String phone) {
        rx.Observable.just(phone)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        //TODO sendCheckCode（String account,int event）event 传入 ？？？
                        JfgCmdInsurance.getCmd().sendCheckCode(phone, JfgEnum.JFG_SMS_REGISTER);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getcheckcode"+throwable.getLocalizedMessage());
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
                        JfgCmdInsurance.getCmd().checkFriendAccount(s);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("checkphoneisbind"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到检测账号的回调
     * @return
     */
    @Override
    public Subscription getCheckPhoneCallback() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckAccountCallback>() {
                    @Override
                    public void call(RxEvent.CheckAccountCallback checkAccountCallback) {
                        if (checkAccountCallback != null && checkAccountCallback instanceof RxEvent.CheckAccountCallback){
                            if (getView() != null){
                                getView().handlerCheckPhoneResult(checkAccountCallback);
                            }
                        }
                    }
                });
    }

    /**
     * 发送修改phone的请求
     * @param userinfo
     */
    @Override
    public void sendChangePhoneReq(final JFGAccount userinfo) {
        rx.Observable.just(userinfo)
                .delay(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<JFGAccount>() {
                    @Override
                    public void call(JFGAccount account) {
                        JfgCmdInsurance.getCmd().setAccount(userinfo);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendChangePhoneReq"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到验证码的回调
     * @return
     */
    @Override
    public Subscription getCheckCodeCallback() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SmsCodeResult.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.SmsCodeResult>() {
                    @Override
                    public void call(RxEvent.SmsCodeResult smsCodeResult) {
                        if (smsCodeResult.error == JError.ErrorOK){
                            PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN,smsCodeResult.token);
                        }
                    }
                });
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getCheckPhoneCallback());
            compositeSubscription.add(getCheckCodeCallback());
        }
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }
    }
}
