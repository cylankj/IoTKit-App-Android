package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGResult;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.sina.AccessTokenKeeper;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/22.
 */

public class AutoLoginTask extends BaseDPTask<BaseDPTaskResult> {
    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        return mDPHelper.getActiveAccount()
                .timeout(1, TimeUnit.SECONDS, Observable.just(null))//说明数据库当前无已激活的 Account
                .observeOn(Schedulers.io())
                .map(account -> {
                    if (account == null) return null;//无已激活的 account ,剩下的操作无需再进行了
                    if (!NetUtils.ping()) {//当前是无网络状态
                        DataSourceManager.getInstance().initFromDB();//无网络情况下从数据库初始化
                    }
                    return account;
                })
                .flatMap(account -> new BaseDPTaskResult().setResultCode(0).setResultResponse(account));
    }

//     try {
//        String aesAccount = PreferencesUtils.getString(JConstant.AUTO_SIGNIN_KEY);
//        AppLogger.d("autoLogin");
//        if (TextUtils.isEmpty(aesAccount)) {
//            AppLogger.d("account is null");
//            return Observable.just(-1);
//        }
//        String decryption = AESUtil.decrypt(aesAccount);
//        AutoSignIn.SignType signType = new Gson().fromJson(decryption, SignType.class);
//        if (signType != null) {
//            StringBuilder pwd = FileUtils.readFile(ContextUtils.getContext().getFilesDir() + File.separator + aesAccount + ".dat", "UTF-8");
//            AppLogger.d("log pwd: ");
//            if (!TextUtils.isEmpty(pwd)) {
//                String finalPwd = AESUtil.decrypt(pwd.toString());
//                if (signType.type == 1) {
//                    RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(-1));
//                    JfgCmdInsurance.getCmd().

//    login(JFGRules.getLanguageType(ContextUtils.getContext()),signType.account,finalPwd);
//                    RxBus.getCacheInstance().postSticky(new RxEvent.ThirdLoginTab(false));
//                } else if (signType.type >= 3) {
//                    //效验本地token是否过期
//                    if (checkTokenOut(signType.type)) {
//                        AppLogger.d("isout:ee");
//                        autoSave(signType.account, 1, "");
//                        return Observable.just(-1);
//                    } else {
//                        AppLogger.d("isout:no");
//                        RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(-1));
//                        JfgCmdInsurance.getCmd().openLogin(JFGRules.getLanguageType(ContextUtils.getContext()),signType.account, finalPwd, signType.type);
//                        RxBus.getCacheInstance().postSticky(new RxEvent.ThirdLoginTab(true));
//                    }
//                }
//                AppLogger.d("log type: " + signType);
//                return Observable.just(0);
//            } else {
//                return Observable.just(-1);
//            }
//        }
//        AppLogger.d("signType is :" + signType);
//        return Observable.just(-1);
//

    @Override
    public Observable<BaseDPTaskResult> performServer(BaseDPTaskResult local) {
        Observable.just(local)
                .subscribeOn(Schedulers.io())
                .filter(ret -> ret.getResultResponse() != null)//如果无 account ,则无继续的必要了
                .map(ret -> (Account) ret.getResultResponse())
                .flatMap(account -> RxBus.getCacheInstance().toObservable(JFGResult.class)
                        .mergeWith(Observable.just("Auto Login Perform Server Start")
                                .observeOn(Schedulers.io())
                                .map(ret -> {
                                            try {
                                                processLogin(account);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            return ret;
                                        }
                                )
                                .flatMap(ret -> RxBus.getCacheInstance().toObservable(JFGResult.class))
                        )
                        .filter(ret -> ret.event == 2 && ret.code == JError.ErrorOK)//登录成功了
                        .first()
                );

        return null;
    }

    private void processLogin(Account account) throws Exception {
        int loginType = account.getLoginType();
        String password = AESUtil.decrypt(account.getPassword());
        if (loginType == 1) {
            JfgCmdInsurance.getCmd().login(JFGRules.getLanguageType(ContextUtils.getContext()), account.getAccount(), password);
        } else if (loginType >= 3) {
//            效验本地token是否过期
            if (checkTokenOut(loginType)) {
                AppLogger.d("isout:ee");
                autoSave(account.getAccount(), 1, "");
            } else {
                AppLogger.d("isout:no");
                RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(-1));
                JfgCmdInsurance.getCmd().openLogin(JFGRules.getLanguageType(ContextUtils.getContext()), account.getAccount(), password, loginType);
                RxBus.getCacheInstance().postSticky(new RxEvent.ThirdLoginTab(true));
            }
        }
    }

    private boolean checkTokenOut(int type) {
        boolean isOut = false;
        switch (type) {
            case 3:
//                isOut = !TencentInstance.getInstance((Activity) ContextUtils.getContext()).mTencent.isSessionValid();
//                AppLogger.d("isout:" + isOut);
                break;
            case 4:
                Oauth2AccessToken oauth2AccessToken = AccessTokenKeeper.readAccessToken(ContextUtils.getContext());
                isOut = !(oauth2AccessToken != null && oauth2AccessToken.isSessionValid());
                break;
            case 6:
//                TwitterSession activeSession = Twitter.getSessionManager().getActiveSession();
//                TwitterAuthToken authToken = activeSession.getAuthToken();
//                if (authToken != null)
//                    isOut = !authToken.isExpired();
                break;
            case 7:
//                isOut = !AccessToken.getCurrentAccessToken().isExpired();
                break;
        }
        return isOut;
    }

    public Observable<Integer> autoSave(String account, int type, String pwd) {
//        return Observable.just("save")
//                .subscribeOn(Schedulers.io())
//                .map(s -> {
//                    try {
//                        SignType signType = new SignType();
//                        signType.account = account;
//                        signType.type = type;
//                        //1.account的aes
//                        String aes = AESUtil.encrypt(new Gson().toJson(signType));
//                        PreferencesUtils.putString(JConstant.AUTO_SIGNIN_KEY, aes);
//                        Log.d(TAG, "account aes: " + aes.length());
//                        //2.保存密码
//                        FileUtils.writeFile(ContextUtils.getContext().getFilesDir() + File.separator + aes + ".dat", AESUtil.encrypt(pwd));
//                        return 0;
//                    } catch (Exception e) {
//                        AppLogger.e("e:" + e.getLocalizedMessage());
//                        return -1;
//                    }
//                });
        return null;
    }
}
