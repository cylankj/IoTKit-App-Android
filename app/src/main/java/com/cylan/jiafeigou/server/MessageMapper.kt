package com.cylan.jiafeigou.server


import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore

data class MIDMessageHeader(var msgId: Int, var caller: String, var callee: String, var seq: Long, var value: Any?)


abstract class DPMessageHeader(@JsonIgnore var msgId: Long = 0, @JsonIgnore var version: Long = 0)
data class VersionValue @JsonCreator constructor(var value: Any? = null) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPStandby @JsonCreator constructor(var standby: Boolean = false, var alarmEnable: Boolean = false, var led: Boolean = false, var autoRecord: Int = 0) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPNet @JsonCreator constructor(var net: Int, var ssid: String) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPTimeZone @JsonCreator constructor(var timezone: String, var offset: Int) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPBindLog @JsonCreator constructor(var isBind: Boolean, var account: String, var oldAccount: String) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPSdcardSummary @JsonCreator constructor(var hasSdcard: Boolean, var errCode: Int) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPSdStatus @JsonCreator constructor(var total: Long = 0, var used: Long = 0, var err: Int = 0, var hasSdcard: Boolean = false) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPAlarmInfo @JsonCreator constructor(var timeStart: Int, var timeEnd: Int) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPAlarm @JsonCreator constructor(var time: Int, var isRecording: Int, var fileIndex: Int, var ossType: Int, var tly: String, var objects: IntArray) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPNotificationInfo @JsonCreator constructor(var notification: Int, var duration: Int) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPTimeLapse @JsonCreator constructor(var timeStart: Int, var timePeriod: Int, var timeDuration: Int, var status: Int) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPCamCoord @JsonCreator constructor(var x: Int, var y: Int, var r: Int) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPBellCallRecord @JsonCreator constructor(var isOK: Int, var time: Int, var duration: Int, var type: Int, var isRecording: Int, var fileIndex: Int) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPWonderItem @JsonCreator constructor(var cid: String, var time: Int, var msgType: Int, var regionType: Int, var fileName: String, var place: String) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPMineMesg @JsonCreator constructor(var cid: String, var isDone: Boolean, var account: String, var sn: String, var pid: Int) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPSystemMesg @JsonCreator constructor(var title: String, var content: String) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPUnreadCount @JsonCreator constructor(var id: Int, var time: Long, var count: String) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class V3DateListReq @JsonCreator constructor(var beginTime: Int, var limit: Int, var asc: Boolean) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPShareItem @JsonCreator constructor(var cid: String, var time: Int, var msgType: Int, var regionType: Int, var fileName: String, var desc: String, var url: String) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPBaseUpgradeStatus @JsonCreator constructor(var upgrade: Int) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DPAutoRecordWatcher(var recordEnable: Boolean) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class DpCoordinate @JsonCreator constructor(var x: Int, var y: Int, var r: Int, var w: Int, var h: Int) : DPMessageHeader()

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
data class BellDeepSleep @JsonCreator constructor(var enable: Boolean, var startTime: Int, var endTime: Int) : DPMessageHeader()