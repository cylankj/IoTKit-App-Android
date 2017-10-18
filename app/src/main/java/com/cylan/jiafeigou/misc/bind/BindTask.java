package com.cylan.jiafeigou.misc.bind;

import android.text.TextUtils;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import rx.functions.Action1;

/**
 * Created by hds on 17-6-7.
 */

public class BindTask implements Action1<Object> {
    @Override
    public void call(Object o) {
        try {
            final String content = PreferencesUtils.getString(JConstant.BINDING_DEVICE);
            if (TextUtils.isEmpty(content)) {
                return;
            }
            UdpConstant.UdpDevicePortrait portrait = new Gson().fromJson(content, UdpConstant.UdpDevicePortrait.class);
            if (portrait != null) {
                BaseApplication.getAppComponent().getCmd().bindDevice(portrait.uuid, portrait.bindCode, portrait.mac, portrait.bindFlag);
                //设备上线后,需要设置时区.设备mac需要持久化,pan摄像头需要用上.
                PreferencesUtils.putString(JConstant.KEY_DEVICE_MAC + portrait.uuid, portrait.mac);
            }
            AppLogger.d("bindTag: 客户端登录,绑定信息:" + portrait);
        } catch (Exception e) {
            AppLogger.d("err: " + e.getLocalizedMessage());
        }
    }
}
