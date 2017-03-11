// APT自动生成的文件,请勿修改!!!!
package com.cylan.jiafeigou.base.module;

import com.cylan.ext.annotations.DPProperty;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import static com.cylan.jiafeigou.dp.DpMsgDefine.DPAlarm;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPAlarmInfo;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPBindLog;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPNet;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPNotificationInfo;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPPrimary;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPSdStatus;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPSdcardSummary;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPSet;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPTimeLapse;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPTimeZone;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DpSdcardFormatRsp;

public class JFGCameraDevice extends Device {
    @DPProperty(msgId = 201)
    public DPNet net;

    @DPProperty(msgId = 204)
    public DPSdStatus sdcard_storage;

    @DPProperty(msgId = 222)
    public DPSet<DPSdcardSummary> sdcard_summary;

    @DPProperty(msgId = 206)
    public DPPrimary<Integer> battery;

    @DPProperty(msgId = 210)
    public DPPrimary<Integer> up_time;

    @DPProperty(msgId = 214)
    public DPTimeZone device_time_zone;

    @DPProperty(msgId = 215)
    public DPPrimary<Boolean> device_rtmp;

    @DPProperty(msgId = 216)
    public DPPrimary<Boolean> device_voltage;

    @DPProperty(msgId = 217)
    public DPPrimary<Boolean> device_mobile_net_priority;

    @DPProperty(msgId = 218)
    public DPPrimary<DpSdcardFormatRsp> device_format_sdcard;

    @DPProperty(msgId = 219)
    public DPBindLog device_bind_log;

    @DPProperty(msgId = 301)
    public DPPrimary<Boolean> device_mic;

    @DPProperty(msgId = 302)
    public DPPrimary<Integer> device_speaker;

    @DPProperty(msgId = 303)
    public DPPrimary<Integer> device_auto_video_record;

    @DPProperty(msgId = 501)
    public DPPrimary<Boolean> camera_alarm_flag;

    @DPProperty(msgId = 502)
    public DPAlarmInfo camera_alarm_info;

    @DPProperty(msgId = 503)
    public DPPrimary<Integer> camera_alarm_sensitivity;

    @DPProperty(msgId = 504)
    public DPNotificationInfo camera_alarm_notification;

    @DPProperty(msgId = 505)
    public DPSet<DPAlarm> camera_alarm_msg;

    @DPProperty(msgId = 506)
    public DPTimeLapse camera_time_lapse_photography;

    @DPProperty(msgId = 508)
    public DpMsgDefine.DPStandby camera_standby;

    @DPProperty(msgId = 509)
    public DPPrimary<Integer> camera_mount_mode;

    @DPProperty(msgId = 510)
    public DPPrimary<Boolean> camera_coordinate;

    @DPProperty(msgId = 512)
    public DPSet<DPAlarm> camera_alarm_msg_v3;

    @DPProperty(msgId = 701)
    public DPPrimary<Boolean> sys_push_flag;

    @Override
    public JFGCameraDevice $() {
        return this;
    }
}
