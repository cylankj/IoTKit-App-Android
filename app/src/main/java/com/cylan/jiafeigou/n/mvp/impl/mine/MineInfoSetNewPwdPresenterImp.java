package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetNewPwdContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/12/28
 * 描述：
 */
public class MineInfoSetNewPwdPresenterImp extends AbstractPresenter<MineInfoSetNewPwdContract.View> implements MineInfoSetNewPwdContract.Presenter {

    public MineInfoSetNewPwdPresenterImp(MineInfoSetNewPwdContract.View view) {
        super(view);
    }

    /**
     * 注册
     *
     * @param account
     * @param pwd
     */
    @Override
    public void openLoginRegister(String account, String pwd, String token) {
        addSubscription(Observable.just("openLoginRegister")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        AppLogger.d("openLoginRegister:" + token);
                        int bindType = TextUtils.isEmpty(token) ? JConstant.TYPE_EMAIL : JConstant.TYPE_PHONE;
                        String bindToken = TextUtils.isEmpty(token) ? "" : token;
                        BaseApplication.getAppComponent().getCmd().setPwdWithBindAccount(pwd, bindType, bindToken);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.OpenLogInSetPwdBack.class).first())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    getView().registerResult(result == null ? -1 : result.jfgResult.code);
                }, e -> {
                    AppLogger.e(e.getMessage());
                }));
    }
}
