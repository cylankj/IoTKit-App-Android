package com.cylan.jiafeigou.n.mvp.impl;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.module.LoginHelper;
import com.cylan.jiafeigou.n.mvp.contract.login.SetupPwdContract;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MD5Util;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

;

/**
 * Created by cylan-hunt on 16-6-30.
 */
public class SetupPwdPresenterImpl extends AbstractPresenter<SetupPwdContract.View>
        implements SetupPwdContract.Presenter {

    public SetupPwdPresenterImpl(SetupPwdContract.View view) {
        super(view);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void register(final String account, final String pwd, final int type, final String token) {
        Subscription subscribe = LoginHelper.performRegisterAction(account, pwd, type, token)
                .timeout(10, TimeUnit.SECONDS, Observable.just(-1))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(code -> {
                    PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, "");
                    getView().submitResult(code);
                }, error -> {
                });
        addDestroySubscription(subscribe);
    }

    @Override
    public void executeLogin(final LoginAccountBean login) {
        LoginHelper.saveUser(login.userName, MD5Util.lowerCaseMD5(login.pwd), 1);
        LoginHelper.performAutoLogin().subscribe(accountArrived -> {
            if (getView() != null) {
                getView().loginResult(JError.ErrorOK);
            }
        }, error -> {
            AppLogger.e(MiscUtils.getErr(error));
        });
    }
}
