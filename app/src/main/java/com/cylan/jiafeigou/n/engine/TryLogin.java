package com.cylan.jiafeigou.n.engine;

import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/4/14.
 */

public class TryLogin {

    public static void tryLogin() {
        //获取到2.x中的账号密码
        PreferencesUtils.init(BaseApplication.getAppComponent().getAppContext(), JConstant.PREF_NAME);
        String account2x = PreferencesUtils.getString(JConstant.KEY_PHONE, "");
        String pwd2x = PreferencesUtils.getString(JConstant.SESSIONID, "");
        PreferencesUtils.putString(JConstant.KEY_PHONE, "");
        PreferencesUtils.putString(JConstant.SESSIONID, "");
        AppLogger.d("account2x:" + account2x);
        Schedulers.io().createWorker().schedule(() -> PreferencesUtils.init(BaseApplication.getAppComponent().getAppContext()));
        if (TextUtils.isEmpty(account2x) || TextUtils.isEmpty(pwd2x)) {
            //正常的流程
            AutoSignIn.getInstance().autoLogin()
                    .flatMap(integer -> {
                        AppLogger.d("integer: " + integer);
                        if (integer == 0) {
                            PreferencesUtils.putInt(JConstant.IS_lOGINED, 1);
                            PreferencesUtils.putBoolean(JConstant.AUTO_SIGNIN_TAB, true);
                            RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultLogin.class)
                                    .subscribeOn(Schedulers.newThread())
                                    .timeout(5, TimeUnit.SECONDS, Observable.just("autoSign in timeout")
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .map(s -> {
                                                AppLogger.d("net type: " + NetUtils.getNetType(ContextUtils.getContext()));
                                                if (NetUtils.getNetType(ContextUtils.getContext()) == -1) {
                                                    RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.NoNet));
                                                } else {
                                                    if (!PreferencesUtils.getBoolean(JConstant.AUTO_lOGIN_PWD_ERR, false)) {
                                                        RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.LoginTimeOut));
                                                    } else {
                                                        PreferencesUtils.putBoolean(JConstant.AUTO_SIGNIN_TAB, false);
                                                    }
                                                }
                                                if (!PreferencesUtils.getBoolean(JConstant.AUTO_lOGIN_PWD_ERR, false)) {
                                                    RxBus.getCacheInstance().hasStickyEvent(RxEvent.ResultLogin.class);
                                                    if (!BaseApplication.getAppComponent().getSourceManager().isOnline()) {
                                                        BaseApplication.getAppComponent().getSourceManager().initFromDB();
                                                    }
                                                } else {
                                                    PreferencesUtils.putBoolean(JConstant.AUTO_SIGNIN_TAB, false);
                                                }
                                                return null;
                                            }))
                                    .subscribe(ret -> {
                                    }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
                        } else if (integer == -1) {
                            //emit failed event.
                            PreferencesUtils.putInt(JConstant.IS_lOGINED, 0);
                            RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.StartLoginPage));
                        }
                        return null;
                    })
                    .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                    .subscribe(ret -> {
                    }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
        } else {
            //直接走引导页
            PreferencesUtils.putInt(JConstant.IS_lOGINED, 1);
            PreferencesUtils.putBoolean(JConstant.UPDATAE_AUTO_LOGIN, true);
            //同时自动登录保存3.0的账号密码
            try {
                BaseApplication.getAppComponent().getCmd().login(JFGRules.getLanguageType(ContextUtils.getContext()), account2x, pwd2x);
                AutoSignIn.getInstance().autoSave(account2x, 1, pwd2x);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }
}
