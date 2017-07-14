package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.Account;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/12
 * 描述：
 */
public class BindMailPresenterImpl extends AbstractPresenter<BindMailContract.View> implements BindMailContract.Presenter {

    public BindMailPresenterImpl(BindMailContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public boolean checkEmail(String email) {
        return JConstant.EMAIL_REG.matcher(email).find();
    }

    /**
     * 发送修改用户属性请求
     */
    @Override
    public void sendSetAccountReq(String newEmail) {
        addSubscription(Observable.just(newEmail)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        JFGAccount jfgAccount = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
                        jfgAccount.setEmail(newEmail);
                        int req = BaseApplication.getAppComponent().getCmd().setAccount(jfgAccount);
                        AppLogger.d("send_setAcc:" + req);
                    } catch (JfgException e) {
                        AppLogger.e("send_setAcc:" + e.getLocalizedMessage());
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.RessetAccountBack.class).first())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resetAccountBack -> {
                    getView().showSendReqResult(resetAccountBack.jfgResult.code);
                }, AppLogger::e), "sendSetAccountReq");

    }

    /**
     * 拿到用户的账号
     *
     * @return
     */
    @Override
    public Account getUserAccount() {
        return BaseApplication.getAppComponent().getSourceManager().getAccount();
    }


    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION};
    }

    @Override
    public boolean isOpenLogin() {
        return BaseApplication.getAppComponent().getSourceManager().getAccount().getLoginType() >= 3;
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        int net = NetUtils.getJfgNetType();
        if (mView != null)
            getView().onNetStateChanged(net);
    }
}
