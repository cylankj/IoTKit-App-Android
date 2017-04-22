package com.cylan.jiafeigou.base.module;

import com.cylan.entity.jniCall.JFGResult;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.engine.AfterLoginService;
import com.cylan.jiafeigou.push.PushPickerIntentService;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscription;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JConstant.KEY_ACCOUNT_LOG_STATE;

/**
 * Created by yanzhendong on 2017/4/14.
 */

@Singleton
public class BaseJFGResultParser {


    @Inject
    public BaseJFGResultParser() {
    }

    public Subscription initSubscription() {
        return RxBus.getCacheInstance().toObservable(JFGResult.class)
                .subscribeOn(Schedulers.io())
                .retry((integer, throwable) -> true)
                .subscribe(this::parserResult, AppLogger::e);
    }


    private void parserResult(JFGResult jfgResult) {
        boolean login = false;
        switch (jfgResult.event) {
            case 0:
                RxBus.getCacheInstance().post(new RxEvent.ResultVerifyCode(jfgResult.code));
                break;
            case 1:
                login = jfgResult.code == JError.ErrorOK;//注册成功
                RxBus.getCacheInstance().post(new RxEvent.ResultRegister(jfgResult.code));
                break;
            case 2:
                login = jfgResult.code == JError.ErrorOK;//登陆成功
                RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(jfgResult.code));
                RxBus.getCacheInstance().post(new RxEvent.ResultUserLogin(jfgResult.code));
                RxBus.getCacheInstance().postSticky(new RxEvent.ResultUpdateLogin(jfgResult.code));
                PreferencesUtils.putInt(KEY_ACCOUNT_LOG_STATE, LogState.STATE_ACCOUNT_ON);
                Account account = BaseApplication.getAppComponent().getSourceManager().getAccount();
                if (account != null) {
                    account.setOnline(true);
                }
                break;
            case JResultEvent.JFG_RESULT_BINDDEV:
                //绑定设备
                RxBus.getCacheInstance().postSticky(new RxEvent.BindDeviceEvent(jfgResult.code));
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
                RxBus.getCacheInstance().post(new RxEvent.SendFeekBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_DEL_FRIEND_ADD_REQ:
                RxBus.getCacheInstance().post(new RxEvent.DeleteAddReqBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_CHECK_REGISTER:
                RxBus.getCacheInstance().post(new RxEvent.CheckRegsiterBack(jfgResult));
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
        if (login) {
            AfterLoginService.startGetAccountAction(ContextUtils.getContext());
            AfterLoginService.startSaveAccountAction(ContextUtils.getContext());
            AfterLoginService.resumeOfflineRequest();
            PushPickerIntentService.start();
        }
    }
}
