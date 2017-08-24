package com.cylan.jiafeigou.server


import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore

data class MIDMessageHeader(var msgId: Int, var caller: String, var callee: String, var seq: Long, var varue: Any?)


abstract class VersionHeader(@JsonIgnore var version: Long = 0)
data class VersionValue(var value: Any? = null) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPStandby(var standby: Boolean = false, var alarmEnable: Boolean = false, var led: Boolean = false, var autoRecord: Int = 0) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPNet(var net: Int, var ssid: String) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPTimeZone(var timezone: String, var offset: Int) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPBindLog(var isBind: Boolean, var account: String, var oldAccount: String) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPSdcardSummary(var hasSdcard: Boolean, var errCode: Int) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPSdStatus(var total: Long = 0, var used: Long = 0, var err: Int = 0, var hasSdcard: Boolean = false) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPAlarmInfo(var timeStart: Int, var timeEnd: Int) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPAlarm(var time: Int, var isRecording: Int, var fileIndex: Int, var ossType: Int, var tly: String, var objects: IntArray) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPNotificationInfo(var notification: Int, var duration: Int) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPTimeLapse(var timeStart: Int, var timePeriod: Int, var timeDuration: Int, var status: Int) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPCamCoord(var x: Int, var y: Int, var r: Int) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPBellCallRecord(var isOK: Int, var time: Int, var duration: Int, var type: Int, var isRecording: Int, var fileIndex: Int) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPWonderItem(var cid: String, var time: Int, var msgType: Int, var regionType: Int, var fileName: String, var place: String) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPMineMesg(var cid: String, var isDone: Boolean, var account: String, var sn: String, var pid: Int) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPSystemMesg(var title: String, var content: String) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPUnreadCount(var id: Int, var time: Long, var count: String) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class V3DateListReq(var beginTime: Int, var limit: Int, var asc: Boolean) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPShareItem(var cid: String, var time: Int, var msgType: Int, var regionType: Int, var fileName: String, var desc: String, var url: String) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPBaseUpgradeStatus(var upgrade: Int) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPAutoRecordWatcher(var recordEnable: Boolean) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DpCoordinate(var x: Int, var y: Int, var r: Int, var w: Int, var h: Int) : VersionHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class BellDeepSleep(var enable: Boolean, var startTime: Int, var endTime: Int) : VersionHeader()