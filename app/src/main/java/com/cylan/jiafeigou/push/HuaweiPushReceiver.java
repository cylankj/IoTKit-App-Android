package com.cylan.jiafeigou.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.huawei.hms.support.api.push.PushReceiver;

/**
 * Created by yanzhendong on 2017/2/24.
 */

public class HuaweiPushReceiver extends PushReceiver {
    @Override
    public boolean onPushMsg(Context context, byte[] bytes, Bundle bundle) {
        AppLogger.e("收到华为推送消息:" + new String(bytes) + bundle);
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
        launchBellLive(cid, null, time);
        return super.onPushMsg(context, bytes, bundle);
    }

    private void launchBellLive(String cid, String url, long time) {
        Intent intent = new Intent(ContextUtils.getContext(), BellLiveActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, cid);
        intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_LISTEN);
        intent.putExtra(JConstant.VIEW_CALL_WAY_EXTRA, url);
        intent.putExtra(JConstant.VIEW_CALL_WAY_TIME, time);
        ContextUtils.getContext().startActivity(intent);
    }
}
