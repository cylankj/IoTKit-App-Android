package com.cylan.jiafeigou.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.push.PushConstant.PUSH_TAG;
import static com.cylan.jiafeigou.push.google.QuickstartPreferences.PUSH_MESSAGE_RESULT;
import static com.cylan.jiafeigou.push.google.QuickstartPreferences.PUSH_MS_NAME;
import static com.cylan.jiafeigou.push.google.QuickstartPreferences.PUSH_TOKEN;
import static com.cylan.jiafeigou.push.google.QuickstartPreferences.REGISTRATION_COMPLETE;
import static com.cylan.jiafeigou.push.google.QuickstartPreferences.SENT_TOKEN_TO_SERVER;

/**
 * Created by hds on 17-4-22.
 */

public class PushResultReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (TextUtils.equals(SENT_TOKEN_TO_SERVER, action)) {

        } else if (TextUtils.equals(REGISTRATION_COMPLETE, action)) {

        } else if (TextUtils.equals(PUSH_MESSAGE_RESULT, action)) {

        } else if (TextUtils.equals(PUSH_TOKEN, action)) {
            String token = intent.getStringExtra(PUSH_TOKEN);
            String from = intent.getStringExtra(PUSH_MS_NAME);
            AppLogger.d(PUSH_TAG + ":token:" + token + ",from:" + from);
            //send to server
            Observable.just(token)
                    .subscribeOn(Schedulers.io())
                    .subscribe(ret -> {
                        try {
                            BaseApplication.getAppComponent().getCmd().setPushToken(ret, context.getPackageName(), 10);
                            AppLogger.e("需要填type");
                        } catch (JfgException e) {
                            AppLogger.e("err:" + MiscUtils.getErr(e));
                        }
                    }, AppLogger::e);
        }
    }
}
