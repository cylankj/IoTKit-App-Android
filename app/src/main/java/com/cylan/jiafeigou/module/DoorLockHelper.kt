package com.cylan.jiafeigou.module

import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.dp.DpMsgMap
import com.cylan.jiafeigou.dp.DpUtils
import com.cylan.jiafeigou.module.message.DPList
import com.cylan.jiafeigou.module.message.DPMessage
import com.cylan.jiafeigou.module.request.RobotForwardDataV3Request
import rx.Observable

/**
 * Created by yanzhendong on 2017/11/16.
 */
object DoorLockHelper {
    const val CHANGE_PASSWORD_ACTION =
            +1 shl 0/*转发给对端:0-否，1-是*/ +
                    +1 shl 1/*get/set:0-get，1-set*/ +
                    +0 shl 2 /*对端应答:0-否，1-是*/ +
                    +0 shl 3/*多终端同步:0-否，1-是*/

    const val OPEN_DOOR_LOCK_ACTION =
            +1 shl 0 /*转发给对端*/ +
                    +1 shl 1 /*set*/ +
                    +1 shl 2/*需要对端应答*/

    const val SET_MONITOR_AREA_ACTION =
            +1 shl 0/*转发给对端:0-否，1-是*/ +
                    +1 shl 1/*get/set:0-get，1-set*/ +
                    +1 shl 2 /*对端应答:0-否，1-是*/ +
                    +1 shl 3/*多终端同步:0-否，1-是*/


    fun changePassword(uuid: String, oldPassword: String, newPassword: String): Observable<Boolean> {
        val dpList = DPList()
        val bytes = DpUtils.pack(DpMsgDefine.DPChangeLockPassword(oldPassword, newPassword))
        dpList.add(DPMessage(DpMsgMap.ID_405_BELL_CHANGE_LOCK_PASSWORD, 0, bytes))
        return RobotForwardDataV3Request(callee = uuid, action = CHANGE_PASSWORD_ACTION, values = dpList)
                .execute()
                .map {
                    it.values.singleOrNull { it.msgId == DpMsgMap.ID_406_BELL_CHANGE_LOCK_PASSWORD_RSP }
                            .let { DpUtils.unpackDataWithoutThrow(it?.value, Int::class.java, -1) } == 0
                }
    }

    fun openDoor(uuid: String, password: String): Observable<Boolean> {
        val dpList = DPList()
        val bytes = DpUtils.pack(DpMsgDefine.DPChangeLockStatusReq(password, 1))
        dpList.add(DPMessage(DpMsgMap.ID_407_BELL_CHANGE_LOCK_STATUS, 0, bytes))
        return RobotForwardDataV3Request(callee = uuid, action = OPEN_DOOR_LOCK_ACTION, values = dpList)
                .execute()
                .map {
                    it.values.singleOrNull { it.msgId == DpMsgMap.ID_408_BELL_CHANGE_LOCK_STATUS_RSP }
                            .let { DpUtils.unpackDataWithoutThrow(it?.value, Int::class.java, 1) } == 0
                }
    }
}