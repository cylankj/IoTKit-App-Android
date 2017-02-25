package com.cylan.jiafeigou.push;

import android.content.Context;
import android.os.Bundle;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.huawei.hms.support.api.push.PushReceiver;

/**
 * Created by yanzhendong on 2017/2/24.
 */

public class HuaweiPushReceiver extends PushReceiver {
    @Override
    public boolean onPushMsg(Context context, byte[] bytes, Bundle bundle) {
        AppLogger.e("收到华为推送消息:" + new String(bytes) + bundle);
        return super.onPushMsg(context, bytes, bundle);
    }

}
