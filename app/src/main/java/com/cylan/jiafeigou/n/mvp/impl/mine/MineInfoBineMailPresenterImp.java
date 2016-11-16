package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoBindMailContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/12
 * 描述：
 */
public class MineInfoBineMailPresenterImp extends AbstractPresenter<MineInfoBindMailContract.View> implements MineInfoBindMailContract.Presenter {

    private CompositeSubscription compositeSubscription;

    public MineInfoBineMailPresenterImp(MineInfoBindMailContract.View view) {
        super(view);
        view.setPresenter(this);

    }

    @Override
    public boolean checkEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    @Override
    public void checkEmailIsBinded(final String email) {
         rx.Observable.just(email)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        JfgCmdInsurance.getCmd().checkFriendAccount(email);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.d("checkEmailIsBinded"+throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public boolean checkAccoutIsPhone(String account) {
        String telRegex = "[1][358]\\d{9}";
        if (account.matches(telRegex)){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 发送修改用户属性请求
     */
    @Override
    public void sendSetAccountReq(JFGAccount account) {
        if (getView() != null){
            getView().showSendReqHint();
        }
        rx.Observable.just(account)
                .delay(2000,TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<JFGAccount>() {
                    @Override
                    public void call(JFGAccount account) {
                        JfgCmdInsurance.getCmd().setAccount(account);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendSetAccountReq"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 检验邮箱是否已经注册过
     * @return
     */
    @Override
    public Subscription getCheckAccountCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class)
                .delay(1000,TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckAccountCallback>() {
                    @Override
                    public void call(RxEvent.CheckAccountCallback checkAccountCallback) {
                        if (checkAccountCallback != null){
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
                        if (getUserInfo != null && getUserInfo instanceof RxEvent.GetUserInfo){
                            getView().showSendReqResult(getUserInfo);
                        }
                    }
                });

    }

    /**
     * 处理检测邮箱是否绑定后结果
     */
    private void handlerCheckAccoutResult(RxEvent.CheckAccountCallback checkAccountCallback) {
        if (checkAccountCallback.i == 0){
            //已经注册过
            if (getView() != null){
                getView().showMailHasBindDialog();
            }
        }else {
            // 没有注册过 查不到账号回调为空
            if (getView() != null){
                getView().showAccountUnReg();
            }
        }
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getCheckAccountCallBack());
            //compositeSubscription.add(getChangeAccountCallBack());
        }
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }
}
