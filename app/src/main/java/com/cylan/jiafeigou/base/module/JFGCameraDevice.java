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
    public DPNet net = DpMsgDefine.EMPTY.NET;

    @DPProperty(msgId = 204)
    public DPSdStatus sdcard_storage = DpMsgDefine.EMPTY.SD_STATUS;

    @DPProperty(msgId = 222)
    public DPSet<DPSdcardSummary> sdcard_summary = DpMsgDefine.EMPTY.DP_SET;

    @DPProperty(msgId = 206)
    public DPPrimary<Integer> battery = DpMsgDefine.EMPTY.DP_INT;

    @DPProperty(msgId = 210)
    public DPPrimary<Integer> up_time = DpMsgDefine.EMPTY.DP_INT;

    @DPProperty(msgId = 214)
    public DPTimeZone device_time_zone = DpMsgDefine.EMPTY.TIME_ZONE;

    @DPProperty(msgId = 215)
    public DPPrimary<Boolean> device_rtmp = DpMsgDefine.EMPTY.DP_BOOL;

    @DPProperty(msgId = 216)
    public DPPrimary<Boolean> device_voltage = DpMsgDefine.EMPTY.DP_BOOL;

    @DPProperty(msgId = 217)
    public DPPrimary<Boolean> device_mobile_net_priority = DpMsgDefine.EMPTY.DP_BOOL;

    @DPProperty(msgId = 218)
    public DpSdcardFormatRsp device_format_sdcard = DpMsgDefine.EMPTY.SDCARD_FORMAT_RSP;

    @DPProperty(msgId = 219)
    public DPBindLog device_bind_log = DpMsgDefine.EMPTY.BIND_LOG;

    @DPProperty(msgId = 301)
    public DPPrimary<Boolean> device_mic = DpMsgDefine.EMPTY.DP_BOOL;

    @DPProperty(msgId = 302)
    public DPPrimary<Integer> device_speaker = DpMsgDefine.EMPTY.DP_INT;

    @DPProperty(msgId = 303)
    public DPPrimary<Integer> device_auto_video_record = DpMsgDefine.EMPTY.DP_INT;

    @DPProperty(msgId = 501)
    public DPPrimary<Boolean> camera_alarm_flag = DpMsgDefine.EMPTY.DP_BOOL;

    @DPProperty(msgId = 502)
    public DPAlarmInfo camera_alarm_info = DpMsgDefine.EMPTY.ALARM_INFO;

    @DPProperty(msgId = 503)
    public DPPrimary<Integer> camera_alarm_sensitivity = DpMsgDefine.EMPTY.DP_INT;

    @DPProperty(msgId = 504)
    public DPNotificationInfo camera_alarm_notification = DpMsgDefine.EMPTY.NOTIFICATION_INFO;

    @DPProperty(msgId = 505)
    public DPSet<DPAlarm> camera_alarm_msg = DpMsgDefine.EMPTY.DP_SET;

    @DPProperty(msgId = 506)
    public DPTimeLapse camera_time_lapse_photography = DpMsgDefine.EMPTY.TIME_LAPSE;

    @DPProperty(msgId = 508)
    public DpMsgDefine.DPStandby camera_standby = DpMsgDefine.EMPTY.STANDBY;

    @DPProperty(msgId = 509)
    public DPPrimary<Integer> camera_mount_mode = DpMsgDefine.EMPTY.DP_INT;

    @DPProperty(msgId = 510)
    public DPPrimary<Boolean> camera_coordinate = DpMsgDefine.EMPTY.DP_BOOL;

    @DPProperty(msgId = 512)
    public DPSet<DPAlarm> camera_alarm_msg_v3 = DpMsgDefine.EMPTY.DP_SET;

    @Override
    public JFGCameraDevice $() {
        return this;
    }
}
