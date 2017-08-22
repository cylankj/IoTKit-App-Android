package com.cylan.jiafeigou.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.huawei.hms.support.api.push.PushReceiver;

import static com.cylan.jiafeigou.push.PushConstant.PUSH_TAG;
import static com.cylan.jiafeigou.push.google.QuickstartPreferences.PUSH_MS_NAME;
import static com.cylan.jiafeigou.push.google.QuickstartPreferences.PUSH_TOKEN;

/**
 * Created by yanzhendong on 2017/2/24.
 */

public class HuaweiPushReceiver extends PushReceiver {

    @Override
    public void onToken(Context context, String token, Bundle extras) {
        String belongId = extras.getString("belongId");
        String content = "获取token和belongId成功，token = " + token + ",belongId = " + belongId;
        AppLogger.d(PUSH_TAG + "HwPush Token success:" + content);
        Intent intent = new Intent();
        intent.setAction(PUSH_TOKEN);
        intent.putExtra(PUSH_TOKEN, token);
        intent.putExtra(PUSH_MS_NAME, "HMS");
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
    }

    @Override
    public void onPushMsg(Context context, byte[] bytes, String s) {
        AppLogger.d(PUSH_TAG + "onPushMsg?" + s + "," + new String(bytes));
    }

    @Override
    public void onPushState(Context context, boolean b) {
        AppLogger.d(PUSH_TAG + "onPushState?" + b);
    }

    @Override
    public boolean onPushMsg(Context context, byte[] bytes, Bundle bundle) {
        String pushMessage = new String(bytes);
        AppLogger.e(PUSH_TAG + "收到华为推送消息:" + pushMessage + "," + bundle + ",context:" + context.getApplicationContext().getPackageName() + ",\n" + context.getApplicationInfo().processName);
        BellPuller.getInstance().fireBellCalling(context, pushMessage, bundle);
        return super.onPushMsg(context, bytes, bundle);
    }


}
