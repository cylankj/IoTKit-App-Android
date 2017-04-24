package com.cylan.jiafeigou.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.huawei.hms.support.api.push.PushReceiver;

import java.net.MalformedURLException;

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
        AppLogger.e("收到华为推送消息:" + new String(bytes) + bundle + ",context:" + context.getApplicationContext().getPackageName());
        JFGSourceManager sourceManager = BaseApplication.getAppComponent().getSourceManager();
        AppLogger.d("push 当前为非登录?" + (sourceManager.getAccount() == null));
        if (sourceManager.getAccount() == null) {
            return true;
        }


        //[16,'500000000385','',1488012270,1]
        String response = new String(bytes);
        if (TextUtils.isEmpty(response)) {
            return true;
        }

        String[] items = response.split(",");
        if (items.length != 5) {
            return true;
        }

        String cid = items[1].replace("\'", "");
        long time = Long.parseLong(items[3]);

        Device device = sourceManager.getDevice(cid);
        if (device == null || !device.available()) {
            AppLogger.d("当前列表没有这个设备");
            return true;
        }
        String url = null;
        try {
            url = new JFGGlideURL(cid, time + ".jpg").toURL().toString();
            AppLogger.d("门铃截图地址:" + url);
        } catch (MalformedURLException e) {
            e.printStackTrace();

        }
        if (System.currentTimeMillis() / 1000L - PreferencesUtils.getInt(JConstant.KEY_NTP_INTERVAL) - time < 30) {
            launchBellLive(cid, url, time);
        }
        return super.onPushMsg(context, bytes, bundle);
    }

    private void launchBellLive(String cid, String url, long time) {
        Intent intent = new Intent(ContextUtils.getContext(), BellLiveActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, cid);
        intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_LISTEN);
        intent.putExtra(JConstant.VIEW_CALL_WAY_EXTRA, url);
        intent.putExtra(JConstant.VIEW_CALL_WAY_TIME, time);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);//华为服务使用.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ContextUtils.getContext().startActivity(intent);
        AppLogger.e("收到华为推送 拉起呼叫界面:");
    }
}
