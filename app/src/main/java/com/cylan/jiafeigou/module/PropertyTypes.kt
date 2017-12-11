@file:Suppress("MemberVisibilityCanPrivate")

package com.cylan.jiafeigou.module

import android.util.SparseArray


/**
 * Created by yzd on 17-12-3.
 *
 *
 *
 */
object PropertyTypes {
    private val propertiesTypes = SparseArray<Class<*>>()
    const val NO_ID = 0
    const val NET_201 = 201
    const val MAC_202 = 202
    const val SDCARD_STORAGE_204 = 204
    const val TIME_ZONE_214 = 214
    const val SDCARD_SUMMARY_222 = 222
    const val BELL_CALL_STATE_401 = 401
    const val BELL_DEEP_SLEEP_404 = 404
    const val CAMERA_ALARM_INFO_502 = 502
    const val CAMERA_ALARM_NOTIFICATION_504 = 504
    const val CAMERA_ALARM_MSG_505 = 505
    const val CAMERA_TIME_LAPSE_PHOTOGRAPHY_506 = 506
    const val CAMERA_STANDBY_508 = 508
    const val CAMERA_COORDINATE_510 = 510
    const val CAMERA_LIVE_RTMP_CTRL_516 = 516
    const val CAMERA_LIVE_RTMP_STATUS_517 = 517
    const val CAMERA_WARNAREA_519 = 519
    const val ACCOUNT_STATE_601 = 601
    const val ACCOUNT_WONDERFUL_MSG_602 = 602
    const val ACCOUNT_WONDERV2_MSG_606 = 606
    const val SYS_PUSH_MESSAGE_701 = 701

    init {
        propertiesTypes.put(NET_201, DPNet::class.java)
        propertiesTypes.put(MAC_202, Int::class.java)
        propertiesTypes.put(SDCARD_STORAGE_204, DPSdStatus::class.java)
        propertiesTypes.put(TIME_ZONE_214, DPTimeZone::class.java)
        propertiesTypes.put(SDCARD_SUMMARY_222, DPSdcardSummary::class.java)
        propertiesTypes.put(BELL_CALL_STATE_401, DPBellCallRecord::class.java)
        propertiesTypes.put(BELL_DEEP_SLEEP_404, DPBellDeepSleep::class.java)
        propertiesTypes.put(CAMERA_ALARM_INFO_502, DPAlarmInfo::class.java)
        propertiesTypes.put(CAMERA_ALARM_NOTIFICATION_504, DPNotificationInfo::class.java)
        propertiesTypes.put(CAMERA_ALARM_MSG_505, DPAlarm::class.java)
        propertiesTypes.put(CAMERA_TIME_LAPSE_PHOTOGRAPHY_506, DPTimeLapse::class.java)
        propertiesTypes.put(CAMERA_STANDBY_508, DPStandby::class.java)
        propertiesTypes.put(CAMERA_COORDINATE_510, DPCoordinate::class.java)
        propertiesTypes.put(CAMERA_LIVE_RTMP_CTRL_516, DPCameraLiveRtmpCtrl::class.java)
        propertiesTypes.put(CAMERA_LIVE_RTMP_STATUS_517, DPCameraLiveRtmpStatus::class.java)
        propertiesTypes.put(CAMERA_WARNAREA_519, DPCameraWarnArea::class.java)
        propertiesTypes.put(ACCOUNT_STATE_601, DPMineMessage::class.java)
        propertiesTypes.put(ACCOUNT_WONDERFUL_MSG_602, DPWonderItem::class.java)
        propertiesTypes.put(ACCOUNT_WONDERV2_MSG_606, DPShareItem::class.java)
        propertiesTypes.put(SYS_PUSH_MESSAGE_701, DPSystemMessage::class.java)
    }

    @JvmStatic
    fun getType(msgId: Int): Class<*>? = propertiesTypes.get(msgId)
}