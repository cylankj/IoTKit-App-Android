package com.cylan.jiafeigou.server

import com.cylan.jiafeigou.dp.DpMsgMap

/**
 * Created by yanzhendong on 2017/8/22.
 */

enum class PAGE_MESSAGE(private val messages: List<Int>) {
    /*这里的消息是针对页面的,不是针对具体的 OS 的 ,还会有一个 OS 属性表,进行筛选*/
    PAGE_HOME(listOf(
            DpMsgMap.ID_201_NET,
            DpMsgMap.ID_202_MAC,
            DpMsgMap.ID_206_BATTERY,
            DpMsgMap.ID_501_CAMERA_ALARM_FLAG,
            DpMsgMap.ID_508_CAMERA_STANDBY_FLAG,
            DpMsgMap.ID_1001_CAM_505_UNREAD_COUNT,
            DpMsgMap.ID_1002_CAM_512UNREAD_COUNT_V2,
            DpMsgMap.ID_1003_CAM_222_UNREAD_COUNT,
            DpMsgMap.ID_1004_BELL_UNREAD_COUNT,
            DpMsgMap.ID_1005_BELL_UNREAD_COUNT_V2
    )),
    PAGE_SETTING(listOf(
            DpMsgMap.ID_201_NET,
            DpMsgMap.ID_202_MAC,
            DpMsgMap.ID_204_SDCARD_STORAGE,
            DpMsgMap.ID_209_LED_INDICATOR,
            DpMsgMap.ID_222_SDCARD_SUMMARY,
            DpMsgMap.ID_216_DEVICE_VOLTAGE,
            DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY,
            DpMsgMap.ID_223_MOBILE_NET,
            DpMsgMap.ID_225_IS_EXIST_NET_WIRED,
            DpMsgMap.ID_226_IS_NET_WIRED,
            DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD,
            DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE,
            DpMsgMap.ID_501_CAMERA_ALARM_FLAG,
            DpMsgMap.ID_508_CAMERA_STANDBY_FLAG,
            DpMsgMap.ID_509_CAMERA_MOUNT_MODE
    )),
    PAGE_CAMERA(listOf(
            DpMsgMap.ID_201_NET,
            DpMsgMap.ID_202_MAC,
            DpMsgMap.ID_204_SDCARD_STORAGE,
            DpMsgMap.ID_207_DEVICE_VERSION,
            DpMsgMap.ID_214_DEVICE_TIME_ZONE,
            DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD,
            DpMsgMap.ID_222_SDCARD_SUMMARY,
            DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD,
            DpMsgMap.ID_501_CAMERA_ALARM_FLAG,
            DpMsgMap.ID_508_CAMERA_STANDBY_FLAG,
            DpMsgMap.ID_509_CAMERA_MOUNT_MODE,
            DpMsgMap.ID_510_CAMERA_COORDINATE,
            DpMsgMap.ID_513_CAM_RESOLUTION,
            DpMsgMap.ID_1001_CAM_505_UNREAD_COUNT,
            DpMsgMap.ID_1002_CAM_512UNREAD_COUNT_V2,
            DpMsgMap.ID_1003_CAM_222_UNREAD_COUNT,
            DpMsgMap.ID_1004_BELL_UNREAD_COUNT,
            DpMsgMap.ID_1005_BELL_UNREAD_COUNT_V2
    )),
    PAGE_SETTING_DETAIL(listOf(
            DpMsgMap.ID_201_NET,
            DpMsgMap.ID_204_SDCARD_STORAGE,
            DpMsgMap.ID_205_CHARGING,
            DpMsgMap.ID_206_BATTERY,
            DpMsgMap.ID_207_DEVICE_VERSION,
            DpMsgMap.ID_208_DEVICE_SYS_VERSION,
            DpMsgMap.ID_210_UP_TIME,
            DpMsgMap.ID_214_DEVICE_TIME_ZONE,
            DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY,
            DpMsgMap.ID_222_SDCARD_SUMMARY
    )),
    PAGE_SAFETY_PROTECTION(listOf(
            DpMsgMap.ID_204_SDCARD_STORAGE,
            DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD,
            DpMsgMap.ID_501_CAMERA_ALARM_FLAG,
            DpMsgMap.ID_502_CAMERA_ALARM_INFO,
            DpMsgMap.ID_503_CAMERA_ALARM_SENSITIVITY,
            DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION,
            DpMsgMap.ID_514_CAM_WARNINTERVAL,
            DpMsgMap.ID_515_CAM_ObjectDetect
    )),
    PAGE_RECORD_SETTING(listOf(
            DpMsgMap.ID_204_SDCARD_STORAGE,
            DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD,
            DpMsgMap.ID_305_VIEW_VIDEO_RECORD,
            DpMsgMap.ID_501_CAMERA_ALARM_FLAG,
            DpMsgMap.ID_508_CAMERA_STANDBY_FLAG
    )),
    PAGE_SDCARD_DETAIL(listOf(
            DpMsgMap.ID_201_NET,
            DpMsgMap.ID_202_MAC,
            DpMsgMap.ID_204_SDCARD_STORAGE
    ));


    fun filter(other: List<Int>?) = if (other == null) messages else messages.filter { it in other }

}

