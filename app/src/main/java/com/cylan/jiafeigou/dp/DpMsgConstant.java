package com.cylan.jiafeigou.dp;

import com.cylan.annotation.Device;
import com.cylan.annotation.DpAnnotation;
import com.cylan.annotation.ForDevice;

/**
 * Created by cylan-hunt on 16-11-8.
 */

public class DpMsgConstant {

    public static final String CAM_BEAN_NAME = "BeanCamInfo";
    public static final String BELL_BEAN_NAME = "BeanBellInfo";
    public static final String MAG_BEAN_NAME = "BeanMagInfo";
    public static final String CLOUD_BEAN_NAME = "BeanCloudInfo";

    public static final int DP_ID_JFG_BEGIN = 200;
    public static final int DP_ID_VIDEO_BEGIN = 300;
    public static final int DP_ID_BELL_BEGIN = 400;
    public static final int DP_ID_CAMERA_BEGIN = 500;
    public static final int DP_ID_ACCOUNT_BEGIN = 600;
    public static final int DP_ID_SYSTEM_BEGIN = 700;

    @DpAnnotation(msgId = 2000001, clazz = String.class)
    public static final String BASE_UUID = null;
    @DpAnnotation(msgId = 2000002, clazz = String.class)
    public static final String BASE_SN = null;
    @DpAnnotation(msgId = 2000003, clazz = String.class)
    public static final String BASE_ALIAS = null;
    @DpAnnotation(msgId = 2000004, clazz = String.class)
    public static final String BASE_PID = null;
    @DpAnnotation(msgId = 2000005, clazz = String.class)
    public static final String BASE_SHARE_ACCOUNT = null;

    //设备归属,需要按照顺序{Camera,Bell,Cloud,Mag}
    @ForDevice(device = {Device.CAMERA, Device.BELL, Device.CLOUD},
            targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME, CLOUD_BEAN_NAME})
    @DpAnnotation(msgId = 201, clazz = DpMsgDefine.MsgNet.class)
    public static final String NET = null;//201

    @ForDevice(device = {Device.CAMERA, Device.BELL, Device.CLOUD, Device.MAG},
            targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME, CLOUD_BEAN_NAME, MAG_BEAN_NAME})
    @DpAnnotation(msgId = 202, clazz = String.class)
    public static final String MAC = null;

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 203, clazz = boolean.class)
    public static final String SDCARD_STATE = null;

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 204, clazz = DpMsgDefine.SdStatus.class)
    public static final String SDCARD_STORAGE = null;

    @ForDevice(device = {Device.CAMERA, Device.BELL, Device.CLOUD},
            targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME, CLOUD_BEAN_NAME})
    @DpAnnotation(msgId = 205, clazz = boolean.class)
    public static final String CHARGING = null;//充电中。。。

    @ForDevice(device = {Device.CAMERA, Device.BELL, Device.CLOUD},
            targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME, CLOUD_BEAN_NAME})
    @DpAnnotation(msgId = 206, clazz = int.class)
    public static final String BATTERY = null;

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 207, clazz = String.class)
    public static final String DEVICE_VERSION = null;

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
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
    @DpAnnotation(msgId = 214, clazz = DpMsgDefine.MsgTimeZone.class)
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
    @DpAnnotation(msgId = 219, clazz = DpMsgDefine.BindLog.class)
    public static final String DEVICE_BIND_LOG = null;

    @DpAnnotation(msgId = 220, clazz = String.class)
    public static final String SDK_VERSION = null;

    @ForDevice(device = {Device.CAMERA, Device.BELL}, targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME})
    @DpAnnotation(msgId = 301, clazz = boolean.class)
    public static final String DEVICE_MIC = null;//301

    @ForDevice(device = {Device.CAMERA, Device.BELL}, targetBeanName = {CAM_BEAN_NAME, BELL_BEAN_NAME})
    @DpAnnotation(msgId = 302, clazz = boolean.class)
    public static final String DEVICE_SPEAKER = null;//302

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 303, clazz = int.class)
    public static final String DEVICE_AUTO_VIDEO_RECORD = null;//303

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 304, clazz = boolean.class)
    public static final String DEVICE_CAMERA_ROTATE = null;//304

    @ForDevice(device = {Device.BELL}, targetBeanName = BELL_BEAN_NAME)
    @DpAnnotation(msgId = 401, clazz = DpMsgDefine.BellCallState.class)
    public static final String BELL_CALL_STATE = null;//门铃呼叫状态

    @ForDevice(device = {Device.BELL}, targetBeanName = BELL_BEAN_NAME)
    @DpAnnotation(msgId = 402, clazz = int.class)
    public static final String BELL_VOICE_MSG = null;//门铃呼叫状态

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 501, clazz = boolean.class)
    public static final String CAMERA_ALARM_FLAG = null;

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 502, clazz = DpMsgDefine.AlarmInfo.class)
    public static final String CAMERA_ALARM_INFO = null;

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 503, clazz = int.class)
    public static final String CAMERA_ALARM_SENSITIVITY = null;

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 504, clazz = DpMsgDefine.NotificationInfo.class)
    public static final String CAMERA_ALARM_NOTIFICATION = null;//报警音效

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 505, clazz = DpMsgDefine.AlarmMsg.class)
    public static final String CAMERA_ALARM_MSG = null;//

    @ForDevice(device = Device.CAMERA, targetBeanName = CAM_BEAN_NAME)
    @DpAnnotation(msgId = 506, clazz = DpMsgDefine.TimeLapse.class)
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

    @DpAnnotation(msgId = 602, clazz = String.class)
    public static final String ACCOUNT_WONDERFUL_MSG = null;//每日精彩消息

    @DpAnnotation(msgId = 701, clazz = boolean.class)
    public static final String SYS_PUSH_FLAG = null;

}
