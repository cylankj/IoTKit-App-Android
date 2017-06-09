package com.cylan.jiafeigou.misc.bind;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import rx.functions.Func1;

/**
 * Created by hds on 17-6-9.
 */

public class SimpleBindTask extends AbstractTask {

    @Override
    protected long getTimeout() {
        return 90 * 1000;
    }

    @Override
    protected boolean successCondition() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(submitResult.uuid);
        return JFGRules.isDeviceOnline(device.$(201, new DpMsgDefine.DPNet()));
    }

    @Override
    protected Func1<String, Boolean> sendInfo() {
        return s -> {
            Device device = BaseApplication.getAppComponent()
                    .getSourceManager().getDevice(submitResult.uuid);
            String content = PreferencesUtils.getString(JConstant.BINDING_DEVICE);
            UdpConstant.UdpDevicePortrait portrait = new Gson().fromJson(content, UdpConstant.UdpDevicePortrait.class);
            AppLogger.d("正在发送绑定请求:" + new Gson().toJson(portrait));
            try {
                if (portrait != null && device == null) {
                    BaseApplication.getAppComponent().getCmd().bindDevice(portrait.uuid, portrait.bindCode, portrait.mac, portrait.bindFlag);
                }
                return true;
            } catch (JfgException e) {
                if (submitListener != null)
                    submitListener.onSubmitErr(BindUtils.BIND_FAILED);
                throw new RxEvent.HelperBreaker("err happen:" + MiscUtils.getErr(e));
            }
        };
    }
}
