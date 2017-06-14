package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.BindMailContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.NetUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/12
 * 描述：
 */
public class BindMailPresenterImpl extends AbstractPresenter<BindMailContract.View> implements BindMailContract.Presenter {

    private boolean isOpenLogin;
    private boolean isSetAcc;

    public BindMailPresenterImpl(BindMailContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public boolean checkEmail(String email) {
        return JConstant.EMAIL_REG.matcher(email).find();
    }

    @Override
    public void isEmailBind(final String email) {
        addSubscription(Observable.just(email)
                .subscribeOn(Schedulers.newThread())
                .delay(2, TimeUnit.SECONDS)
                .flatMap(s -> {
                    try {
                        int ret = BaseApplication.getAppComponent().getCmd().checkFriendAccount(email);
                        return Observable.just(ret);
                    } catch (JfgException e) {
                        return Observable.just(-1);
                    }
                })
                .flatMap(integer -> RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class))
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(checkAccountCallback -> {
                    if (checkAccountCallback.i == 0) {
                        //已经注册过
                        getView().showMailHasBindDialog();
                    } else {
                        // 没有注册过
                        sendSetAccountReq(email);
                    }
                }, AppLogger::e), "isEmailBind");
    }

    /**
     * 发送修改用户属性请求
     */
    @Override
    public void sendSetAccountReq(String newEmail) {
        addSubscription(rx.Observable.just(newEmail)
                .subscribeOn(Schedulers.newThread())
                .subscribe(newEmail1 -> {
                    try {
                        JFGAccount jfgAccount = getUserAccount();
                        jfgAccount.resetFlag();
                        jfgAccount.setEmail(newEmail1);
                        int req = BaseApplication.getAppComponent().getCmd().setAccount(jfgAccount);
                        isSetAcc = true;
                        AppLogger.d("send_setAcc:" + req);
                    } catch (JfgException e) {
                        AppLogger.e("send_setAcc:" + e.getLocalizedMessage());
                    }
                }, AppLogger::e), "sendSetAccountReq");

    }

    /**
     * 修改属性后的回调
     */
    @Override
    public Subscription getAccountCallBack() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountArrived -> getView().getUserAccountData(accountArrived.jfgAccount),
                        e -> AppLogger.d(e.getMessage()));
    }

    /**
     * 拿到用户的账号
     *
     * @return
     */
    @Override
    public JFGAccount getUserAccount() {
        return BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
    }


    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION};
    }

//    /**
//     * 是否三方登录
//     *
//     * @return
//     */
//    @Override
//    public Subscription isOpenLoginBack() {
//        return RxBus.getCacheInstance().toObservableSticky(RxEvent.ThirdLoginTab.class)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(thirdLoginTab -> {
//                    isOpenLogin = thirdLoginTab.isThird;
//                }, e -> AppLogger.d(e.getMessage()));
//    }

    @Override
    public boolean isOpenLogin() {
        return isOpenLogin;
    }

    @Override
    public Subscription changeAccountBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.RessetAccountBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resetAccountBack -> {
                    if (isSetAcc) {
                        getView().showSendReqResult(resetAccountBack.jfgResult.code);
                        isSetAcc = false;
                    }
                }, AppLogger::e);
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        int net = NetUtils.getJfgNetType();
        if (mView != null)
            getView().onNetStateChanged(net);
    }


    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                getAccountCallBack(),
                changeAccountBack()
        };
    }
}
