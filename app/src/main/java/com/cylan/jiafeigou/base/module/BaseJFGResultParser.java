package com.cylan.jiafeigou.base.module;

import com.cylan.entity.jniCall.JFGResult;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.misc.MethodFilter;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.task.FetchFeedbackTask;
import com.cylan.jiafeigou.n.task.FetchFriendsTask;
import com.cylan.jiafeigou.n.task.SysUnreadCountTask;
import com.cylan.jiafeigou.push.PushPickerIntentService;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JConstant.KEY_ACCOUNT_LOG_STATE;

/**
 * Created by yanzhendong on 2017/4/14.
 */

@Singleton
public class
BaseJFGResultParser {


    @Inject
    public BaseJFGResultParser() {
    }

    public Subscription initSubscription() {
        return RxBus.getCacheInstance().toObservable(JFGResult.class)
                .subscribeOn(Schedulers.io())
                .retry((integer, throwable) -> true)
                .subscribe(ret -> {
                    try {
                        parserResult(ret);
                    } catch (Throwable throwable) {
                    }
                }, AppLogger::e);
    }


    private void parserResult(JFGResult jfgResult) {
        boolean login = false;
        switch (jfgResult.event) {
            case 0:
                RxBus.getCacheInstance().post(new RxEvent.ResultVerifyCode(jfgResult.code).setResult(jfgResult));
                break;
            case 1:
                login = jfgResult.code == JError.ErrorOK;//注册成功

                RxBus.getCacheInstance().post(new RxEvent.ResultRegister(jfgResult.code));
                break;
            case 2:
                login = jfgResult.code == JError.ErrorOK;//登陆成功
                AppLogger.w("登录成功了");
                //最短3s种一次。
                if (MethodFilter.run("parserResultLoginSuc", 5 * 1000)) {
                    if (login) {
                        BaseApplication.getAppComponent().getCmd().getAccount();
                        PushPickerIntentService.start();
                        Schedulers.io().createWorker().schedule(() -> {
                            new FetchFriendsTask().call("");
                            new FetchFeedbackTask().call("");
                            new SysUnreadCountTask().call("");
                        });

                        PreferencesUtils.putInt(KEY_ACCOUNT_LOG_STATE, LogState.STATE_ACCOUNT_ON);
                        PreferencesUtils.putBoolean(JConstant.AUTO_lOGIN_PWD_ERR, false);
                    }
                    RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(jfgResult.code));
                }
                break;
            case JResultEvent.JFG_RESULT_BINDDEV:
                //绑定设备
//                RxBus.getCacheInstance().post(new RxEvent.BindDeviceEvent(jfgResult.code));
                break;
            case JResultEvent.JFG_RESULT_UNBINDDEV:
                RxBus.getCacheInstance().post(new RxEvent.UnBindDeviceEvent(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_CHANGE_PASS:
                RxBus.getCacheInstance().post(new RxEvent.ChangePwdBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_RESET_PASS:
                RxBus.getCacheInstance().post(new RxEvent.ResetPwdBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_ADD_FRIEND:
                RxBus.getCacheInstance().post(new RxEvent.AddFriendBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_CONSENT_ADD_FRIEND:
                RxBus.getCacheInstance().post(new RxEvent.ConsentAddFriendBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_DEL_FRIEND:
                RxBus.getCacheInstance().post(new RxEvent.DelFriendBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_SETPWD_WITH_BINDACCOUNT:
                RxBus.getCacheInstance().post(new RxEvent.OpenLogInSetPwdBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_SEND_FEEDBACK:
                RxBus.getCacheInstance().post(new RxEvent.SendFeedBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_DEL_FRIEND_ADD_REQ:
                RxBus.getCacheInstance().post(new RxEvent.DeleteAddReqBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_CHECK_REGISTER:
                RxBus.getCacheInstance().post(new RxEvent.CheckRegisterBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_SET_DEVICE_ALIAS:
                RxBus.getCacheInstance().post(new RxEvent.SetAlias(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_SET_FRIEND_MARKNAME:
                RxBus.getCacheInstance().post(new RxEvent.SetFriendMarkNameBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_UPDATE_ACCOUNT:
                RxBus.getCacheInstance().post(new RxEvent.RessetAccountBack(jfgResult));
                break;
        }
        if (BaseApplication.isBackground()) {
            return;
        }
//        if (login && MethodFilter.run("PushPickerIntentService", 5 * 1000)) {
//            PushPickerIntentService.start();
//            Observable.just(new FetchFeedbackTask(),
//                    new FetchFriendsTask(),
//                    new SysUnreadCountTask())
//                    .subscribeOn(Schedulers.io())
//                    .subscribe(objectAction1 -> objectAction1.call(""), AppLogger::e);
//        }
        AndroidSchedulers.mainThread().createWorker().schedule(() -> {
            switch (jfgResult.code) {
                case JError.ErrorConnect:
                    ToastUtil.showNegativeToast(getString(R.string.LOGIN_ERR));
                    break;
                case JError.ErrorP2PSocket:
                    ToastUtil.showNegativeToast(getString(R.string.NoNetworkTips));
                    break;
            }
        });
    }

    private String getString(int id) {
        return ContextUtils.getContext().getString(id);
    }
}
