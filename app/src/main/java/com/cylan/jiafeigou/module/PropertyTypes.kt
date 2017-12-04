@file:Suppress("MemberVisibilityCanPrivate")

package com.example.yzd.helloworld

import android.util.SparseArray
import com.cylan.jiafeigou.module.*


/**
 * Created by yzd on 17-12-3.
 *
 *
 *
 */
object PropertyTypes {
    private val propertiesTypes = SparseArray<Class<*>>()

    const val NET_201 = 201
    const val MAC_202 = 202
    const val SDCARD_STORAGE_204 = 204
    const val TIME_ZONE_214 = 214
    const val SDCARD_SUMMARY_222 = 222
    const val CAMERA_ALARM_INFO_502 = 502
    const val CAMERA_ALARM_MSG_505 = 505
    const val CAMERA_STANDBY_508 = 508

    init {
        propertiesTypes.put(NET_201, DPNet::class.java)
        propertiesTypes.put(MAC_202, Int::class.java)
        propertiesTypes.put(SDCARD_STORAGE_204, DPSdStatus::class.java)
        propertiesTypes.put(TIME_ZONE_214, DPTimeZone::class.java)
        propertiesTypes.put(SDCARD_SUMMARY_222, DPSdcardSummary::class.java)
        propertiesTypes.put(CAMERA_ALARM_INFO_502, DPAlarmInfo::class.java)
        propertiesTypes.put(CAMERA_ALARM_MSG_505, DPAlarm::class.java)
        propertiesTypes.put(CAMERA_STANDBY_508, DPStandby::class.java)

    }

    @JvmStatic
    fun getType(msgId: Int): Class<*>? = propertiesTypes.get(msgId)
}