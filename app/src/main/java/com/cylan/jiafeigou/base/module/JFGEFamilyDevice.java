// APT自动生成的文件,请勿修改!!!!
package com.cylan.jiafeigou.base.module;

import com.cylan.ext.annotations.DPType;
import com.cylan.jiafeigou.cache.db.module.Device;

import static com.cylan.jiafeigou.dp.DpMsgDefine.DPNet;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPTimeZone;

public class JFGEFamilyDevice extends Device {
    @DPProperty(type = DPNet.class)
    public static final int NET = 201;

    @DPProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int BATTERY = 206;

    @DPProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int UP_TIME = 210;

    @DPProperty(type = DPTimeZone.class)
    public static final int DEVICE_TIME_ZONE = 214;
}
