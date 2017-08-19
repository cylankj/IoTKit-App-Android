package com.cylan.jiafeigou.server

import com.google.gson.JsonArray
import org.msgpack.core.MessageBufferPacker
import org.msgpack.core.MessagePack
import org.msgpack.core.MessageUnpacker


/**
 * Created by yanzhendong on 2017/8/19.
 */
@SuppressWarnings("unused")
object MessageMapper {

    fun getPrimary(jsonArray: JsonArray?) = jsonArray?.get(2)

    fun getArray(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray

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

        fun pack(net: Int = 0, ssid: String = "") {}

        fun getNet(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(0)?.asInt ?: 0

        fun getSSID(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(1)?.asString ?: ""
    }

    object DPIDBaseMac {
        val KEY = 202
        fun getMac(jsonArray: JsonArray?) = jsonArray?.get(2)?.asString ?: ""
    }

    object DPIDBaseFormatSDAck {
        val KEY = 203

        fun getStorage(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(0)?.asLong ?: 0

        fun getStorageUsed(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(1)?.asLong ?: 0

        fun getSdcardErrno(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(2)?.asInt ?: 0

        fun getSdcard(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(3)?.asBoolean ?: false
    }

    object DPIDBaseSDStatus {
        val KEY = 204

        fun getStorage(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(0)?.asLong ?: 0

        fun getStorageUsed(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(1)?.asLong ?: 0

        fun getSdcardErrno(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(2)?.asInt ?: 0

        fun getSdcard(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(3)?.asBoolean ?: false
    }

    object DPIDBasePower {
        val KEY = 205

        fun getPower(jsonArray: JsonArray?) = jsonArray?.get(2)?.asBoolean ?: false
    }

    object DPIDBaseBattery {
        val KEY = 206

        fun getBattery(jsonArray: JsonArray?) = jsonArray?.get(2)?.asInt ?: 0
    }

    object DPIDBaseVersion {
        val KEY = 207

        fun getVersion(jsonArray: JsonArray?) = jsonArray?.get(2)?.asString ?: ""
    }

    object DPIDBaseSysVersion {
        val KEY = 208

        fun getSysVersion(jsonArray: JsonArray?) = jsonArray?.get(2)?.asString ?: ""
    }

    object DPIDBaseLED {
        val KEY = 209

        fun getLed(jsonArray: JsonArray?) = jsonArray?.get(2)?.asBoolean ?: false
    }

    object DPIDBaseUptime {
        val KEY = 210

        fun getPowerOn(jsonArray: JsonArray?) = jsonArray?.get(2)?.asInt ?: 0
    }

    object DPIDBaseCidLog {
        val KEY = 212

        fun getLog(jsonArray: JsonArray?) = jsonArray?.get(2)?.asString ?: ""
    }

    object DPIDBaseP2PVersion {
        val KEY = 213

        fun getVersion(jsonArray: JsonArray?) = jsonArray?.get(2)?.asInt ?: 0
    }

    object DPIDBaseTimezone {
        val KEY = 214

        fun getTimezone(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(0)?.asString ?: ""

        fun getOffset(jsonArray: JsonArray?) = jsonArray?.get(2)?.asJsonArray?.get(1)?.asInt ?: 0
    }

    object DPIDBaseIsPushFlow {
        val KEY = 215

        fun getIsPushFlow(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asBoolean ?: false
    }

    object DPIDBaseIsNTSC {
        val KEY = 216

        fun getIsNTSC(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asBoolean ?: false
    }

    object DPIDBaseIsMobile {
        val KEY = 217

        fun getIsMobile(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asBoolean ?: false
    }

    object DPIDBaseFormatSD {
        val KEY = 218

        fun getValue(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDBaseBind {
        val KEY = 219

        fun getIsBind(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asBoolean ?: false

        fun getAccount(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asString ?: ""

        fun getOldAccount(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asString ?: ""
    }

    object DPIDBaseSdkVersion {
        val KEY = 220

        fun getVersion(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asString ?: ""
    }

    object DPIDBaseCtrlLog {
        val KEY = 221

        fun getIp(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asString ?: ""

        fun getPort(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0

        fun getTimeEnd(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asInt ?: 0
    }

    object DPIDBaseSDInfoOnOff {
        val KEY = 222

        fun getSdcard(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asBoolean ?: false

        fun getSdcardErrno(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0
    }

    object DPIDBaseIsExistMobile {
        val KEY = 223

        fun getSim(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDBaseCtrlLogUploadFile {
        val KEY = 224

        fun getFilename(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asString ?: ""
    }

    object DPIDBaseIsExistNetWired {
        val KEY = 225

        fun getValue(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDBaseIsNetWired {
        val KEY = 226

        fun getIsWiredNet(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asBoolean ?: false

    }

    object DPIDBasePrivateIP {
        val KEY = 227

        fun getIP(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asString ?: ""
    }

    object DPIDBaseUpgradeStatus {
        val KEY = 228

        fun getValue(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDBaseVoltage {
        val KEY = 229

        fun getVoltage(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDBaseRegion {
        val KEY = 230

        fun getRegion(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDVideoMic {
        val KEY = 301

        fun getMike(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asBoolean ?: false
    }

    object DPIDVideoSpeaker {
        val KEY = 302

        fun getSpeaker(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asBoolean ?: false
    }

    object DPIDVideoAutoRecord {
        val KEY = 303

        fun getAutoRecord(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDVideoDirection {
        val KEY = 304

        fun getDirection(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDViewVideoRecord {
        val KEY = 305

        fun getRecordEnable(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asBoolean ?: false
    }

    object DPIDBellCallMsg {
        val KEY = 401

        fun getIsOK(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asInt ?: 0

        fun getTime(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0

        fun getTimeDuration(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asInt ?: 0

        fun getRegionType(jsonArray: JsonArray?) = getArray(jsonArray)?.get(3)?.asInt ?: 0

        fun getIsRecord(jsonArray: JsonArray?) = getArray(jsonArray)?.get(4)?.asInt ?: 0
    }

    object DPIDBellLeaveMsg {
        val KEY = 402

        fun getValue(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDBellCallMsgV3 {
        val KEY = 403

        fun getIsOK(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asInt ?: 0

        fun getTime(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0

        fun getTimeDuration(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asInt ?: 0

        fun getRegionType(jsonArray: JsonArray?) = getArray(jsonArray)?.get(3)?.asInt ?: 0

        fun getIsRecord(jsonArray: JsonArray?) = getArray(jsonArray)?.get(4)?.asInt ?: 0

    }

    object DPIDBellDeepSleep {
        val KEY = 404

        fun getEnbale(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asBoolean ?: false

        fun getBeginTime(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asLong ?: 0

        fun getEndTime(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asLong ?: 0
    }

    object DPIDCameraWarnEnable {
        val KEY = 501

        fun getEnable(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asBoolean ?: false
    }

    object DPIDCameraWarnTime {
        val KEY = 502

        fun getBeginTime(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asInt ?: 0

        fun getEndTime(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0

        fun getWeek(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asInt ?: 0
    }

    object DPIDCameraWarnSensitivity {
        val KEY = 503

        fun getSensitivity(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDCameraWarnSound {
        val KEY = 504

        fun getSound(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asInt ?: 0

        fun getSoundLong(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0
    }

    object DPIDCameraWarnMsg {
        val KEY = 505

        fun getTime(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asInt ?: 0

        fun getIsRecord(jsonArray: JsonArray?) = getArray(jsonArray)?.get(1)?.asInt ?: 0

        fun getFile(jsonArray: JsonArray?) = getArray(jsonArray)?.get(2)?.asInt ?: 0

        fun getRegionType(jsonArray: JsonArray?) = getArray(jsonArray)?.get(3)?.asInt ?: 0

        fun getTly(jsonArray: JsonArray?) = getArray(jsonArray)?.get(4)?.asString ?: ""

        fun getObjects(jsonArray: JsonArray?) = getArray(jsonArray)?.get(5)?.asJsonArray ?: JsonArray(0)

        fun getHumanNum(jsonArray: JsonArray?) = getArray(jsonArray)?.get(6)?.asInt ?: 0
    }

    object DPIDCameraTimeLapse {
        val KEY = 506

        fun getBeginTime(jsonArray: JsonArray?) = getArray(jsonArray)?.get(0)?.asInt ?: 0

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

        fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDCountUnReadAccountBind {
        val KEY = 1101

        fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDCountUnReadAccountWonder {
        val KEY = 1102

        fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDCountUnReadAccountShare {
        val KEY = 1103

        fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDCountUnReadAccountIsShared {
        val KEY = 1104

        fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }

    object DPIDCountUnReadSystemMsg {
        val KEY = 1105

        fun getCount(jsonArray: JsonArray?) = getPrimary(jsonArray)?.asInt ?: 0
    }



}