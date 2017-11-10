package com.cylan.jiafeigou.module

import android.net.Uri
import android.util.Log
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.n.base.BaseApplication

/**
 * Created by yanzhendong on 2017/11/10.
 * schema 为 cylan
 * 举个例子: cylan:///7day/0001/18503060168/AI/290100000002/1509621188_1.jpg?regionType=1
 */
object SchemeResolver {
    const val CYLAN_SCHEMA = "cylan"
    const val REGION_TYPE = "regionType"
    private val TAG = SchemeResolver::javaClass.name
    private val ALIYUN_HOST_SUFFIX = "aliyuncs.com"
    fun accept(schema: String): Boolean {
        val uri = Uri.parse(schema)
        return CYLAN_SCHEMA == uri.scheme || uri.host.contains(ALIYUN_HOST_SUFFIX)
    }

    fun build(schema: String): GlideUrl {
        val builder = LazyHeaders.Builder()
        val parse = Uri.parse(schema)
        return when {
            parse.host.contains(ALIYUN_HOST_SUFFIX) -> {
                val appCmd = BaseApplication.getAppComponent().getCmd()
                val regionType = parse.getQueryParameter(REGION_TYPE).toIntOrNull() ?: 0
                val signedCloudUrl = appCmd.getSignedCloudUrl(regionType, parse.path)
                SchemaGlideUrl(schema.replace(parse.host, ""), signedCloudUrl, null)
            }
            CYLAN_SCHEMA == parse.scheme -> {
                val regionType = parse.getQueryParameter(REGION_TYPE).toIntOrNull() ?: 0
                val appCmd = BaseApplication.getAppComponent().getCmd()
                val signedCloudUrl = appCmd.getSignedCloudUrl(regionType, parse.path)
                SchemaGlideUrl(schema, signedCloudUrl, Headers.DEFAULT)
            }
            else -> {
                GlideUrl(schema)
            }
        }.apply {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "request:$this")
            }
        }
    }

    private class SchemaGlideUrl(
            private val schema: String,
            url: String?,
            headers: Headers?
    ) : GlideUrl(url ?: "http://www.cylan.com.cn", headers ?: Headers.DEFAULT) {
        override fun getCacheKey(): String {
            return schema
        }
    }

    enum class ImageType {
        TYPE_WARM,
        TYPE_BELL_V2,
        TYPE_WARM_AI

    }

    fun parse(imageType: ImageType, cid: String, vid: String?, account: String, name: String): String {
        return ""
//        when (imageType) {
//            ImageType.TYPE_WARM -> {
//
//            }
//            ImageType.TYPE_WARM_AI -> {
//
//            }
//        }
//
//        return when {
//            dpAlarm.face_id?.isNotEmpty() == true -> {
//                "$CYLAN_SCHEMA:///long/$vid/$account/AI/$cid/%${dpAlarm.time}.jpg?$REGION_TYPE=${dpAlarm.ossType}"
//            }
//            else -> {
//                "$CYLAN_SCHEMA:///cid/$vid/$cid/${dpAlarm.time}_$index.jpg?$REGION_TYPE=${dpAlarm.ossType}"
//            }
//        }
    }

    fun parse(cid: String, vid: String?, bellRecord: DpMsgDefine.DPBellCallRecord): String {
        return ""
//        val name = if (bellRecord.fileIndex == -1) {
//            "${bellRecord.time}.jpg"
//        } else {
//            "${bellRecord.time}_${bellRecord.fileIndex}.jpg"
//        }
//        when {
//            vid.isNullOrEmpty() -> {
//                //旧版
//                "$CYLAN_SCHEMA:///$cid/$name?$REGION_TYPE=${bellRecord.type}"
//            }
//            bellRecord.fileIndex == -1 -> {
//
//            }
//            else -> {
//
//            }
//        }
//
//        if (dpBellCallRecord.fileIndex == -1) {
//            //旧版本门铃呼叫记录,不带index
//            result = CamWarnGlideURL(cid, dpBellCallRecord.time + ".jpg", dpBellCallRecord.type)
//        } else {
//            result = CamWarnGlideURL(cid, dpBellCallRecord.time + "_" + index + ".jpg", dpBellCallRecord.time, index, dpBellCallRecord.type)
//        }
    }
}