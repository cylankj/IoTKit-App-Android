package com.cylan.jiafeigou;

import com.cylan.annotation.DPMessage;
import com.cylan.annotation.Device;
import com.cylan.annotation.DpAnnotation;
import com.cylan.annotation.ForDevice;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import static com.cylan.annotation.DPTarget.CAMERA;
import static com.cylan.annotation.DPTarget.DOORBELL;
import static com.cylan.annotation.DPTarget.EFAMILY;
import static com.cylan.annotation.DPTarget.MAGNETOMETER;
import static com.cylan.annotation.DPType.TYPE_PRIMARY;
import static com.cylan.jiafeigou.dp.DpMsgConstant.BELL_BEAN_NAME;
import static com.cylan.jiafeigou.dp.DpMsgConstant.CAM_BEAN_NAME;
import static com.cylan.jiafeigou.dp.DpMsgConstant.CLOUD_BEAN_NAME;

/**
 * Created by yzd on 17-1-14.
 */

public class DPConstant {
    //设备归属,需要按照顺序{Camera,Bell,Cloud,Mag}

    @DPMessage(name = "net", primaryType = DpMsgDefine.DPNet.class, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int NET = 201;

    @DPMessage(name = "mac", primaryType = String.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL, EFAMILY, MAGNETOMETER})
    public static final int MAC = 202;

    @DPMessage(name = "sdcard_storage", primaryType = DpMsgDefine.DPSdStatus.class)
    public static final int SDCARD_STORAGE = 204;

    @DPMessage(name = "sdcard_summary", primaryType = DpMsgDefine.DPSdcardSummary.class)
    public static final int SDCARD_SUMMARY = 222;

    @DPMessage(name = "charging", primaryType = Boolean.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int CHARGING = 205;//充电中。。。

    @DPMessage(name = "battery", primaryType = Integer.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int BATTERY = 206;

    @ForDevice(device = {Device.CAMERA, Device.BELL}, targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME})
    @DpAnnotation(msgId = 207, clazz = String.class)
    public static final String DEVICE_VERSION = null;

    @ForDevice(device = {Device.CAMERA, Device.BELL}, targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME})
    @DpAnnotation(msgId = 208, clazz = String.class)
    public static final String DEVICE_SYS_VERSION = null;

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 209, clazz = boolean.class)
    public static final String LED_INDICATOR = null;

    @ForDevice(device = {Device.CAMERA, Device.BELL, Device.CLOUD},
            targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME, CLOUD_BEAN_NAME})
    @DpAnnotation(msgId = 210, clazz = int.class)
    public static final String UP_TIME = null;


    @DpAnnotation(msgId = 211, clazz = int.class)
    public static final String APP_UPLOAD_LOG = null;

    @DpAnnotation(msgId = 212, clazz = String.class)
    public static final String DEVICE_UPLOAD_LOG = null;

    @DpAnnotation(msgId = 213, clazz = int.class)
    public static final String DEVICE_P2P_VERSION = null;

    @ForDevice(device = {Device.CAMERA, Device.BELL, Device.CLOUD},
            targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME, CLOUD_BEAN_NAME})
    @DpAnnotation(msgId = 214, clazz = DpMsgDefine.DPTimeZone.class)
    public static final String DEVICE_TIME_ZONE = null;

    @DpAnnotation(msgId = 215, clazz = boolean.class)
    public static final String DEVICE_RTMP = null;

    @ForDevice(device = {Device.CAMERA, Device.BELL}, targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME})
    @DpAnnotation(msgId = 216, clazz = boolean.class)
    public static final String DEVICE_VOLTAGE = null;

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 217, clazz = boolean.class)
    public static final String DEVICE_MOBILE_NET_PRIORITY = null;

    @DpAnnotation(msgId = 218, clazz = Void.class)
    public static final String DEVICE_FORMAT_SDCARD = null;

    @ForDevice(device = {Device.CAMERA, Device.BELL}, targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME})
    @DpAnnotation(msgId = 219, clazz = DpMsgDefine.DPBindLog.class)
    public static final String DEVICE_BIND_LOG = null;

    @DpAnnotation(msgId = 220, clazz = String.class)
    public static final String SDK_VERSION = null;

    @ForDevice(device = {Device.CAMERA, Device.BELL}, targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME})
    @DpAnnotation(msgId = 301, clazz = boolean.class)
    public static final String DEVICE_MIC = null;//301

    @ForDevice(device = {Device.CAMERA, Device.BELL}, targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME})
    @DpAnnotation(msgId = 302, clazz = int.class)
    public static final String DEVICE_SPEAKER = null;//302

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 303, clazz = int.class)
    public static final String DEVICE_AUTO_VIDEO_RECORD = null;//303

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 304, clazz = int.class)
    public static final String DEVICE_CAMERA_ROTATE = null;//304

    @ForDevice(device = {Device.BELL}, targetBeanName = BELL_BEAN_NAME)
    @DpAnnotation(msgId = 401, clazz = DpMsgDefine.DPBellCallRecord.class)
    public static final String BELL_CALL_STATE = null;//门铃呼叫状态

    @ForDevice(device = {Device.BELL}, targetBeanName = BELL_BEAN_NAME)
    @DpAnnotation(msgId = 402, clazz = int.class)
    public static final String BELL_VOICE_MSG = null;//门铃呼叫状态

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 501, clazz = boolean.class)
    public static final String CAMERA_ALARM_FLAG = null;

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 502, clazz = DpMsgDefine.DPAlarmInfo.class)
    public static final String CAMERA_ALARM_INFO = null;

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 503, clazz = int.class)
    public static final String CAMERA_ALARM_SENSITIVITY = null;

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 504, clazz = DpMsgDefine.DPNotificationInfo.class)
    public static final String CAMERA_ALARM_NOTIFICATION = null;//报警音效

    //    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)暂时去掉,这是一个列表消息.可能包含大量的数据
    @DpAnnotation(msgId = 505, clazz = DpMsgDefine.DPAlarm.class)
    public static final String CAMERA_ALARM_MSG = null;//

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 506, clazz = DpMsgDefine.DPTimeLapse.class)
    public static final String CAMERA_TIME_LAPSE_PHOTOGRAPHY = null;//

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 508, clazz = boolean.class)
    public static final String CAMERA_STANDBY_FLAG = null;//是否开启直播，待机模式

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 509, clazz = int.class)
    public static final String CAMERA_MOUNT_MODE = null;//针对全景摄像头，吊顶，挂壁

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 510, clazz = boolean.class)
    public static final String CAMERA_COORDINATE = null;//视频坐标

    @DpAnnotation(msgId = 601, clazz = String.class)
    public static final String ACCOUNT_STATE = null;//绑定、解绑消息

    @DpAnnotation(msgId = 602, clazz = DpMsgDefine.DPWonderItem.class)
    public static final String ACCOUNT_WONDERFUL_MSG = null;//每日精彩消息

    @DpAnnotation(msgId = 701, clazz = boolean.class)
    public static final String SYS_PUSH_FLAG = null;

}
