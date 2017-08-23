package com.cylan.jiafeigou.server

import com.cylan.jiafeigou.server.cache.PropertyItem
import com.fasterxml.jackson.annotation.JsonCreator
import com.google.gson.JsonArray
import org.msgpack.core.MessageBufferPacker
import org.msgpack.core.MessagePack
import org.msgpack.core.MessageUnpacker


/**
 * Created by yanzhendong on 2017/8/19.
 */
@SuppressWarnings("unused")

fun getPrimary(jsonArray: JsonArray?) = jsonArray?.get(2)

fun getArray(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray

fun getBodyAsList(value: Any?) = ((value as? List<*>)?.getOrNull(2) as? List<*>)

fun getBodyAsAny(value: Any?) = (value as? List<*>)?.getOrNull(2)

private val unpacker: ThreadLocal<MessageUnpacker>  by lazy {
    object : ThreadLocal<MessageUnpacker>() {
        override fun initialValue() = MessagePack.newDefaultUnpacker(byteArrayOf())
    }
}

private val packer: ThreadLocal<MessageBufferPacker> by lazy {
    object : ThreadLocal<MessageBufferPacker>() {
        override fun initialValue() = MessagePack.newDefaultBufferPacker()
    }
}


//class H constructor(header: Array<Any>) {
//
//}


data class MIDMessageHeader(val msgId: Int, val caller: String, val callee: String, val seq: Long, var body: Any) {
    @JsonCreator constructor(header: List<Any>) : this(header[0] as Int, header[1] as String, header[2] as String, header[3] as Long, header[4])
}

data class H(var a: Int, var caller: String, var callee: String, var seq: Long, var body: Any)

object DPIDRelayServer {
    val KEY = 1

    fun getValue(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asString ?: ""
}

object DPIDHeartbeat {
    val KEY = 2
    fun getHeartbeat(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDCloudStorage {
    val KEY = 3
}

object DPIDClientUpgradeConfig {
    val KEY = 5

    fun getUrl(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asString ?: ""

    fun getIsUpgrade(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0
}


object DPIDBaseNet {
    val KEY = 201

    val NET_INT_KEY = 0

    val SSID_STRING_KEY = 1

    fun pack(net: Int = 0, ssid: String = "") {}

    fun getNet(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(0)?.asInt ?: 0

    fun getSSID(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(1)?.asString ?: ""
}

object DPIDBaseMac {
    val KEY = 202

    val PRIMARY_STRING = null
    fun getMac(jsonArray: JsonArray?) = jsonArray?.get(2)?.asString ?: ""
}

object DPIDBaseFormatSDAck {
    val KEY = 203

    val STORAGE_LONG_KEY = 0
    val STORAGE_USED_LONG_KEY = 1
    val SDCARD_ERR_NO_INT_KEY = 2
    val SDCARD_BOOLEAN_KEY = 3


    fun getStorage(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(0)?.asLong ?: 0

    fun getStorageUsed(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(1)?.asLong ?: 0

    fun getSdcardErrno(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(2)?.asInt ?: 0

    fun getSdcard(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(3)?.asBoolean ?: false
}

object DPIDBaseSDStatus {
    val KEY = 204

    val STORAGE_LONG_KEY = 0
    val STORAGE_USED_LONG_KEY = 1
    val SDCARD_ERR_NO_INT_KEY = 2
    val SDCARD_BOOLEAN_KEY = 3

    fun getStorage(value: Any?, defaultValue: Long) = getBodyAsList(value)?.getOrNull(0) as? Long ?: defaultValue

    fun getStorageUsed(value: Any?, defaultValue: Long) = getBodyAsList(value)?.getOrNull(1) as?Long ?: defaultValue

    fun getSdcardErrno(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(2) as? Int ?: defaultValue

    fun getSdcard(value: Any?, defaultValue: Boolean) = getBodyAsList(value)?.getOrNull(3) as? Boolean ?: defaultValue
}

object DPIDBasePower {
    val KEY = 205

    fun getPower(value: Any?, defaultValue: Boolean) = getBodyAsAny(value) as? Boolean ?: defaultValue
}

object DPIDBaseBattery {
    val KEY = 206

    fun getBattery(value: Any?, defaultValue: Int) = getBodyAsAny(value) as? Int ?: defaultValue
}

object DPIDBaseVersion {
    val KEY = 207

    fun getVersion(value: Any?, defaultValue: String) = getBodyAsAny(value) as? String ?: defaultValue  //jsonArray?.get(2)?.asString ?: ""
}

object DPIDBaseSysVersion {
    val KEY = 208

    fun getSysVersion(value: Any?, defaultValue: String) = getBodyAsAny(value) as? String ?: defaultValue  //jsonArray?.get(2)?.asString ?: ""
}

object DPIDBaseLED {
    val KEY = 209

    fun getLed(value: Any?, defaultValue: Boolean) = getBodyAsAny(value) as? Boolean ?: defaultValue  //jsonArray?.get(2)?.asBoolean ?: false
}

object DPIDBaseUptime {
    val KEY = 210

    fun getPowerOn(value: Any?, defaultValue: Int) = getBodyAsAny(value) as? Int ?: defaultValue //jsonArray?.get(2)?.asInt ?: 0
}

object DPIDBaseCidLog {
    val KEY = 212

    fun getLog(value: Any?, defaultValue: String) = getBodyAsAny(value) as? String ?: defaultValue// jsonArray?.get(2)?.asString ?: ""
}

object DPIDBaseP2PVersion {
    val KEY = 213

    fun getVersion(value: Any?, defaultValue: Int) = getBodyAsAny(value) as? Int ?: defaultValue //jsonArray?.get(2)?.asInt ?: 0
}

object DPIDBaseTimezone {
    val KEY = 214

    fun getTimezone(value: Any?, defaultValue: String) = getBodyAsList(value)?.getOrNull(0) as? String ?: defaultValue //jsonArray?.get(2)?.asJsonArray?.get(0)?.asString ?: ""

    fun getOffset(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(1) as? Int ?: defaultValue //jsonArray?.get(2)?.asJsonArray?.get(1)?.asInt ?: 0
}

object DPIDBaseIsPushFlow {
    val KEY = 215

    fun getIsPushFlow(value: Any?, defaultValue: Boolean) = getBodyAsAny(value) as? Boolean ?: defaultValue //getPrimary(jsonArray)?.asBoolean ?: false
}

object DPIDBaseIsNTSC {
    val KEY = 216

    fun getIsNTSC(value: Any?, defaultValue: Boolean) = getBodyAsAny(value) as? Boolean ?: defaultValue //getPrimary(jsonArray)?.asBoolean ?: false
}

object DPIDBaseIsMobile {
    val KEY = 217

    fun getIsMobile(value: Any?, defaultValue: Boolean) = getBodyAsAny(value) as? Boolean ?: defaultValue //getPrimary(jsonArray)?.asBoolean ?: false
}

object DPIDBaseFormatSD {
    val KEY = 218

    fun getValue(value: Any?, defaultValue: Int) = getBodyAsAny(value) as? Int ?: defaultValue //getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDBaseBind {
    val KEY = 219

    fun getIsBind(value: Any?, defaultValue: Boolean) = getBodyAsList(value)?.getOrNull(0) as? Boolean ?: defaultValue //getArray(jsonArray)?.get(0)?.asBoolean ?: false

    fun getAccount(value: Any?, defaultValue: String) = getBodyAsList(value)?.getOrNull(1) as? String ?: defaultValue //getArray(jsonArray)?.get(1)?.asString ?: ""

    fun getOldAccount(value: Any?, defaultValue: String) = getBodyAsList(value)?.getOrNull(2) as? String ?: defaultValue //getArray(jsonArray)?.get(2)?.asString ?: ""
}

object DPIDBaseSdkVersion {
    val KEY = 220

    fun getVersion(value: Any?, defaultValue: String) = getBodyAsAny(value) as? String ?: defaultValue //getPrimary(jsonArray)?.asString ?: ""
}

object DPIDBaseCtrlLog {
    val KEY = 221

    fun getIp(value: Any?, defaultValue: String) = getBodyAsList(value)?.getOrNull(0) as? String ?: defaultValue //getArray(jsonArray)?.get(0)?.asString ?: ""

    fun getPort(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(1) as? Int ?: defaultValue //getArray(jsonArray)?.get(1)?.asInt ?: 0

    fun getTimeEnd(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(2) as? Int ?: defaultValue //getArray(jsonArray)?.get(2)?.asInt ?: 0
}

object DPIDBaseSDInfoOnOff {
    val KEY = 222

    fun getSdcard(value: Any?, defaultValue: Boolean) = getBodyAsList(value)?.getOrNull(0) as? Boolean ?: defaultValue //getArray(jsonArray)?.get(0)?.asBoolean ?: false

    fun getSdcardErrno(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(1) as? Int ?: defaultValue //getArray(jsonArray)?.get(1)?.asInt ?: 0
}

object DPIDBaseIsExistMobile {
    val KEY = 223

    fun getSim(value: Any?, defaultValue: Int) = getBodyAsAny(value) as?Int ?: defaultValue  //getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDBaseCtrlLogUploadFile {
    val KEY = 224

    fun getFilename(value: Any?, defaultValue: String) = getBodyAsAny(value) as? String ?: defaultValue //getPrimary(jsonArray)?.asString ?: ""
}

object DPIDBaseIsExistNetWired {
    val KEY = 225

    fun getValue(value: Any?, defaultValue: Int) = getBodyAsAny(value) as?Int ?: defaultValue //getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDBaseIsNetWired {
    val KEY = 226

    fun getIsWiredNet(value: Any?, defaultValue: Boolean) = getBodyAsAny(value) as?Boolean ?: defaultValue //getPrimary(jsonArray)?.asBoolean ?: false

}

object DPIDBasePrivateIP {
    val KEY = 227

    fun getIP(value: Any?, defaultValue: String) = getBodyAsAny(value) as?String ?: defaultValue  //getPrimary(jsonArray)?.asString ?: ""
}

object DPIDBaseUpgradeStatus {
    val KEY = 228

    fun getValue(value: Any?, defaultValue: Int) = getBodyAsAny(value) as?Int ?: defaultValue //getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDBaseVoltage {
    val KEY = 229

    fun getVoltage(value: Any?, defaultValue: Int) = getBodyAsAny(value) as?Int ?: defaultValue //getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDBaseRegion {
    val KEY = 230

    fun getRegion(value: Any?, defaultValue: Int) = getBodyAsAny(value) as? Int ?: defaultValue //getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDVideoMic {
    val KEY = 301

    fun getMike(value: Any?, defaultValue: Boolean) = getBodyAsAny(value) as?Boolean ?: defaultValue //getPrimary(jsonArray)?.asBoolean ?: false
}

object DPIDVideoSpeaker {
    val KEY = 302

    fun getSpeaker(value: Any?, defaultValue: Boolean) = getBodyAsAny(value) as?Boolean ?: defaultValue //getPrimary(jsonArray)?.asBoolean ?: false
}

object DPIDVideoAutoRecord {
    val KEY = 303

    fun getAutoRecord(value: Any?, defaultValue: Int) = getBodyAsAny(value) as? Int ?: defaultValue  //getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDVideoDirection {
    val KEY = 304

    fun getDirection(value: Any?, defaultValue: Int) = getBodyAsAny(value) as? Int ?: defaultValue //getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDViewVideoRecord {
    val KEY = 305

    fun getRecordEnable(value: Any?, defaultValue: Boolean) = getBodyAsAny(value) as?Boolean ?: defaultValue //getPrimary(jsonArray)?.asBoolean ?: false
}

object DPIDBellCallMsg {
    val KEY = 401

    fun getIsOK(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(0) as?Int ?: defaultValue //getArray(jsonArray)?.get(0)?.asInt ?: 0

    fun getTime(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(1) as?Int ?: defaultValue // getArray(jsonArray)?.get(1)?.asInt ?: 0

    fun getTimeDuration(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(2) as? Int ?: defaultValue //getArray(jsonArray)?.get(2)?.asInt ?: 0

    fun getRegionType(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(3) as?Int ?: defaultValue //getArray(jsonArray)?.get(3)?.asInt ?: 0

    fun getIsRecord(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(4) as? Int ?: defaultValue //getArray(jsonArray)?.get(4)?.asInt ?: 0
}

object DPIDBellLeaveMsg {
    val KEY = 402

    fun getValue(value: Any?, defaultValue: Int) = getBodyAsAny(value) as? Int ?: defaultValue //getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDBellCallMsgV3 {
    val KEY = 403

    fun getIsOK(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(0) as?Int ?: defaultValue //getArray(jsonArray)?.get(0)?.asInt ?: 0

    fun getTime(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(1) as?Int ?: defaultValue //getArray(jsonArray)?.get(1)?.asInt ?: 0

    fun getTimeDuration(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(2) as?Int ?: defaultValue  //getArray(jsonArray)?.get(2)?.asInt ?: 0

    fun getRegionType(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(3) as? Int ?: defaultValue //getArray(jsonArray)?.get(3)?.asInt ?: 0

    fun getIsRecord(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(4) as? Int ?: defaultValue //getArray(jsonArray)?.get(4)?.asInt ?: 0

}

object DPIDBellDeepSleep {
    val KEY = 404

    fun getEnbale(value: Any?, defaultValue: Boolean) = getBodyAsList(value)?.getOrNull(0) as? Boolean ?: defaultValue //getArray(jsonArray)?.get(0)?.asBoolean ?: false

    fun getBeginTime(value: Any?, defaultValue: Long) = getBodyAsList(value)?.getOrNull(1) as? Long ?: defaultValue //getArray(jsonArray)?.get(1)?.asLong ?: 0

    fun getEndTime(value: Any?, defaultValue: Long) = getBodyAsList(value)?.getOrNull(2) as? Long ?: defaultValue //getArray(jsonArray)?.get(2)?.asLong ?: 0
}

object DPIDCameraWarnEnable {
    val KEY = 501

    fun getEnable(value: Any?, defaultValue: Boolean) = getBodyAsAny(value) as?Boolean ?: defaultValue  //getPrimary(jsonArray)?.asBoolean ?: false
}

object DPIDCameraWarnTime {
    val KEY = 502

    fun getBeginTime(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(0) as? Int ?: defaultValue  //getArray(jsonArray)?.get(0)?.asInt ?: 0

    fun getEndTime(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(1) as? Int ?: defaultValue //getArray(jsonArray)?.get(1)?.asInt ?: 0

    fun getWeek(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(2) as? Int ?: defaultValue  //getArray(jsonArray)?.get(2)?.asInt ?: 0
}

object DPIDCameraWarnSensitivity {
    val KEY = 503

    fun getSensitivity(value: Any?, defaultValue: Int) = getBodyAsAny(value) as? Int ?: defaultValue //getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDCameraWarnSound {
    val KEY = 504

    fun getSound(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(0) as? Int ?: defaultValue  //getArray(jsonArray)?.get(0)?.asInt ?: 0

    fun getSoundLong(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(1) as? Int ?: defaultValue  //getArray(jsonArray)?.get(1)?.asInt ?: 0
}

object DPIDCameraWarnMsg {
    val KEY = 505

    fun getTime(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(0) as? Int ?: defaultValue //getArray(jsonArray)?.get(0)?.asInt ?: 0

    fun getIsRecord(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(1) as? Int ?: defaultValue  //getArray(jsonArray)?.get(1)?.asInt ?: 0

    fun getFile(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(2) as? Int ?: defaultValue //getArray(jsonArray)?.get(2)?.asInt ?: 0

    fun getRegionType(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(3) as? Int ?: defaultValue  //getArray(jsonArray)?.get(3)?.asInt ?: 0

    fun getTly(value: Any?, defaultValue: String) = getBodyAsList(value)?.getOrNull(4) as? String ?: defaultValue //getArray(jsonArray)?.get(4)?.asString ?: ""

    fun getObjects(value: Any?, defaultValue: List<Int>) = getBodyAsList(value)?.getOrNull(5) as? List<*> ?: defaultValue  //getArray(jsonArray)?.get(5)?.asJsonArray ?: JsonArray(0)

    fun getHumanNum(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(6) as? Int ?: defaultValue//getArray(jsonArray)?.get(6)?.asInt ?: 0
}

object DPIDCameraTimeLapse {
    val KEY = 506

    fun getBeginTime(value: Any?, defaultValue: Int) = getBodyAsList(value)?.getOrNull(0) as? Int ?: defaultValue //getArray(jsonArray)?.get(0)?.asInt ?: 0

    fun getTimeCycle(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0

    fun getTimeDuration(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asInt ?: 0

    fun getStatus(jsonArray: JsonArray?) = getArray(jsonArray)?.get(3)?.asInt ?: 0
}

object DPIDCameraStandby {
    val KEY = 508

    fun getStandby(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asBoolean ?: false

    fun getWarnEnable(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asBoolean ?: false

    fun getLed(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asBoolean ?: false

    fun getAutoRecord(jsonArray: JsonArray?) = getArray(jsonArray)?.get(3)?.asInt ?: 0
}

object DPIDCameraHangMode {
    val KEY = 509

    fun getTly(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asString ?: ""
}

object DPIDCameraCoord {
    val KEY = 510

    fun getX(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asInt ?: 0
    fun getY(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0
    fun getR(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asInt ?: 0
    fun getW(jsonArray: JsonArray?) = getArray(jsonArray)?.get(3)?.asInt ?: 0
    fun getH(jsonArray: JsonArray?) = getArray(jsonArray)?.get(4)?.asInt ?: 0
}

object DPIDCameraWarnAndWonder {
    val KEY = 511

    fun getCtimeMsec(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asLong ?: 0
}

object DPIDCameraWarnMsgV3 {
    val KEY = 512

    fun getTime(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asInt ?: 0
    fun getIsRecord(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0
    fun getFile(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asInt ?: 0
    fun getRegionType(jsonArray: JsonArray?) = getArray(jsonArray)?.get(3)?.asInt ?: 0
    fun getTly(jsonArray: JsonArray?) = getArray(jsonArray)?.get(4)?.asString ?: ""

}

object DPIDCameraResolution {
    val KEY = 513

    fun getResolution(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDCameraWarnInterval {
    val KEY = 514

    fun getSec(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDCameraObjectDetect {
    val KEY = 515

    fun getObjects(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asJsonArray ?: JsonArray(0)
}

object DPIDAccountBind {
    val KEY = 601

    fun getCid(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asString ?: ""

    fun getIsBind(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asBoolean ?: false

    fun getAccount(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asString ?: ""

    fun getSn(jsonArray: JsonArray?) = getArray(jsonArray)?.get(3)?.asString ?: ""

    fun getPid(jsonArray: JsonArray?) = getArray(jsonArray)?.get(4)?.asInt ?: 0
}

object DPIDAccountWonder {
    val KEY = 602

    fun getCid(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asString ?: ""

    fun getTime(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0

    fun getMsgType(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asInt ?: 0

    fun getRegionType(jsonArray: JsonArray?) = getArray(jsonArray)?.get(3)?.asInt ?: 0

    fun getFileName(jsonArray: JsonArray?) = getArray(jsonArray)?.get(4)?.asString ?: ""

    fun getAlias(jsonArray: JsonArray?) = getArray(jsonArray)?.get(5)?.asString ?: ""

    fun getFavoriteTimeMsec(jsonArray: JsonArray?) = getArray(jsonArray)?.get(6)?.asLong ?: 0
}

object DPIDAccountShare {
    val KEY = 603

    fun getCid(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asString ?: ""

    fun getIsShare(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asBoolean ?: false

    fun getAccount(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asString ?: ""
}

object DPIDAccountIsShared {
    val KEY = 604

    fun getCid(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asString ?: ""

    fun getIsShare(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asBoolean ?: false

    fun getAccount(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asString ?: ""
}

object DPIDAccountLog {
    val KEY = 605

    fun getValue(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asString ?: ""
}

object DPIDAccountWonderV2 {
    val KEY = 606

    val CID_STRING_KEY = 0
    val TIME_INT_KEY = 1

    fun getCid(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asString ?: ""

    fun getTime(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0

    fun getMsgType(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asInt ?: 0

    fun getRegionType(jsonArray: JsonArray?) = getArray(jsonArray)?.get(3)?.asInt ?: 0

    fun getFileName(jsonArray: JsonArray?) = getArray(jsonArray)?.get(4)?.asString ?: ""

    fun getDesc(jsonArray: JsonArray?) = getArray(jsonArray)?.get(5)?.asString ?: ""

    fun getUrl(jsonArray: JsonArray?) = getArray(jsonArray)?.get(6)?.asString ?: ""
}

object DPIDCountUnReadCameraWarnMsg {
    val KEY = 1001

    fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDCountUnReadCameraWarnMsgV3 {
    val KEY = 1002

    fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDCountUnReadBaseSDInfo {
    val KEY = 1003

    fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDCountUnReadBellCallMsg {
    val KEY = 1004

    fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDCountUnReadBellCallMsgV3 {
    val KEY = 1005
    val COUNT_INT_PRIMARY = null
    fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDCountUnReadAccountBind {
    val KEY = 1101
    val COUNT_INT_PRIMARY = null
    fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDCountUnReadAccountWonder {
    val KEY = 1102
    val COUNT_INT_PRIMARY = null
    fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDCountUnReadAccountShare {
    val KEY = 1103
    val COUNT_INT_PRIMARY = null
    fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
}

object DPIDCountUnReadAccountIsShared {
    val KEY = 1104
    fun getCount(propertyItem: PropertyItem?, defaultValue: Int) = propertyItem?.asInt(defaultValue) ?: defaultValue
}

object DPIDCountUnReadSystemMsg {
    val KEY = 1105
    fun getCount(propertyItem: PropertyItem?, defaultValue: Int) = propertyItem?.asInt(defaultValue) ?: defaultValue
}
