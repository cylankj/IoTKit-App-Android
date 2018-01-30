package com.cylan.jiafeigou.module

import android.net.Uri
import android.util.Log
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.dp.BaseDataPoint
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.dp.DpMsgMap
import com.cylan.jiafeigou.support.OptionsImpl
import com.cylan.jiafeigou.support.log.AppLogger
import java.net.URL

/**
 * Created by yanzhendong on 2017/11/10.
 * schema 为 cylan
 * 举个例子: cylan:///7day/0001/18503060168/AI/290100000002/1509621188_1.jpg?regionType=1
 */
object SchemeResolver {
    const val CYLAN_SCHEMA = "cylan"
    const val REGION_TYPE = "regionType"
    private val TAG = SchemeResolver::class.java.canonicalName
    private val ALIYUN_HOST_SUFFIX = "aliyuncs.com"
    fun accept(schema: String): Boolean {
        val uri = Uri.parse(schema)
        return CYLAN_SCHEMA == uri.scheme
//                || uri.host.contains(ALIYUN_HOST_SUFFIX)
    }

    fun build(schema: String): GlideUrl {
        val builder = LazyHeaders.Builder()
        val parse = Uri.parse(schema)
        return when {
//            parse.host.contains(ALIYUN_HOST_SUFFIX) -> {
//                val appCmd = BaseApplication.getAppComponent().getCmd()
//                val regionType = parse.getQueryParameter(REGION_TYPE).toIntOrNull() ?: 0
//                val signedCloudUrl = appCmd.getSignedCloudUrl(regionType, parse.path)
//                SchemaGlideUrl(schema.replace(parse.host, ""), signedCloudUrl, null)
//            }
            CYLAN_SCHEMA == parse.scheme -> {
                SchemaGlideUrl(schema, Headers.DEFAULT)
            }
            else -> {
                GlideUrl(schema)
            }
        }
    }

    private class SchemaGlideUrl(
            private val schema: String,
            headers: Headers?
    ) : GlideUrl(schema, headers ?: Headers.DEFAULT) {
        override fun getCacheKey(): String {
            return schema
        }

        override fun toStringUrl() = try {
            val parse = Uri.parse(schema)
            val regionType = parse.getQueryParameter(REGION_TYPE).toIntOrNull() ?: 0
            val appCmd = Command.getInstance()
            val signedCloudUrl = appCmd.getSignedCloudUrl(regionType, parse.path)
            AppLogger.w("SchemeResolver:cylan scheme:$schema, signed: $signedCloudUrl")
            signedCloudUrl
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }!!

        override fun toURL() = URL(toStringUrl())

        override fun getHeaders(): MutableMap<String, String> {
            //TODO:可以通过 header 签名的方式授权
            return super.getHeaders()
        }
    }

    fun parse(cid: String, message: BaseDataPoint): String {
        val vid = OptionsImpl.getVid()
        val device = DataSourceManager.getInstance().getDevice(cid)
        val account = DataSourceManager.getInstance().account?.account ?: ""
        val resolver: String = when (message.getMsgId().toInt()) {
            DpMsgMap.ID_505_CAMERA_ALARM_MSG -> {
                val dpAlarm = message as DpMsgDefine.DPAlarm
                when {
//                    dpAlarm.face_id?.isNotEmpty() == true -> "cylan:///long/$vid/$account/AI/$cid/${dpAlarm.time}.jpg?regionType=${dpAlarm.ossType}"
                    dpAlarm.face_id?.isNotEmpty() == true -> "cylan:///long/$account/AI/$cid/${dpAlarm.time}.jpg?regionType=${dpAlarm.ossType}"
                    device.vid.isNullOrEmpty() -> "cylan:///$cid/${dpAlarm.time}.jpg?regionType=${dpAlarm.ossType}"
                    else -> "cylan:///cid/${device.vid}/$cid/${dpAlarm.time}?regionType=${dpAlarm.ossType}"
                }
            }
            DpMsgMap.ID_401_BELL_CALL_STATE -> {
                val dpBellCallRecord = message as DpMsgDefine.DPBellCallRecord
                when {
                    device.vid.isNullOrEmpty() -> "cylan:///$cid/${dpBellCallRecord.time}.jpg?regionType=${dpBellCallRecord.type}"
                    else -> "cylan:///$cid/%${device.vid}/$cid/${dpBellCallRecord.time}?regionType=${dpBellCallRecord.type}"
                }.apply { index(this, dpBellCallRecord.fileIndex) }
            }
            DpMsgMap.ID_526_CAM_AI_WARM_MSG -> {
                val dpAlarm = message as DpMsgDefine.DPCameraAIWarmMsg
                "cylan:///long/$account/AI/$cid/${dpAlarm.time}.jpg?regionType=${dpAlarm.regionType}"
            }
            else -> ""
        }
        Log.i(TAG, "SchemeResolver:parse for cid:$cid,message:$message,result is:$resolver")
        return resolver
    }

    fun index(origin: String, index: Int): String {
        if (index == -1) return origin
        val result = "\\d+.jpg".toRegex().find(origin)
        return if (result != null) {
            val value = result.value
            origin.replace(value, value.replace(".jpg", "_$index.jpg"))
        } else {
            origin
        }
    }
}