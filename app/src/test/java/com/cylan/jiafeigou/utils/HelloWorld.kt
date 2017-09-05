package com.cylan.jiafeigou.utils

import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.server.MIDMessageHeader
import com.cylan.jiafeigou.server.cache.objectMapper
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import org.msgpack.jackson.dataformat.MessagePackFactory

/**
 * Created by yanzhendong on 2017/8/23.
 */
class HelloWorld {

    @Test
    fun testTuple() {
        val mapper = ObjectMapper(MessagePackFactory())


        var header = arrayOf(20000, "www", "ggg", 8476979L, arrayOf("AAA", "BBB"))

        val bytes = mapper.writeValueAsBytes(header)

        val readValue1 = mapper.readValue(byteArrayOf(-110, -75, 116, 101, 115, 116, 32, 32, 116, 101, 115, 116, 32, 32, 116, 101, 115, 116, 32, 116, 101, 115, 116, -80, 49, 50, 51, 52, 53, 54, 55, 56, 57, 60, 112, 62, 60, 47, 112, 62), Any::class.java)
//        val readValue = mapper.readValue(bytes, List::class.java)
//        var (a, b) = body as List<*>
//
//        print("$msgId ,$caller,$callee,$seq,$a , $b")

        println(readValue1)

    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    data class SS(val a: String, val b: String)

    @Test
    fun testT() {
        val mapper = jacksonObjectMapper()

        var header = arrayOf(20000, "www", "ggg", 8476979L, arrayOf("AAA", "BBB"))

        val midMessageHeader = MIDMessageHeader(304, "GGGG", "EEEE", 796969, arrayOf("AAA", "BBB"))

        val asBytes = mapper.writeValueAsBytes(midMessageHeader)

        val bytes = mapper.writeValueAsBytes(header)

        var ss = mapper.readValue<MIDMessageHeader>(asBytes)

//        val value = mapper.convertValue<SS>(ss.body!!)
//
//        println("$value")

    }

//    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
//    data class SSS(var value: Any? = null) : VersionHeader()
//
//
//    /**只针对 array 类型的数据才需要 cast, 如果是原始类型,比如 int string, 直接 asInt ,asString 就行了**/
//    fun <T : Any> cast(propertyItem: PropertyItem, defaultValue: T): T {
//        return try {
//            objectMapper.get().convertValue(propertyItem.value, defaultValue::class.java).apply { (this as? VersionHeader)?.version = propertyItem.version }
//        } catch (e: Exception) {
//            println(e.message)
//            defaultValue
//        }
//    }

    @Test
    fun testA() {
        val mapper = objectMapper.get()

        val dpStandby = mapper.convertValue(arrayListOf(false, false, false, 2), DpMsgDefine.DPStandby::class.java)

        println(dpStandby)

        print(DpMsgDefine.DPStandby::class.java.fields)
        print("")
    }

    @Test
    fun testM() {

        val mapper = ObjectMapper(MessagePackFactory())
        val response = "[2516,'500000002756','',1504082734,'http://oss-cn-hangzhou.aliyuncs.com/jiafeigou-test/500000002756/1504082734.jpg?OSSAccessKeyId=xjBdwD1du8lf2wMI&Expires=1504687544&Signature=zuii8SpBgGRcdiA%2F8xA7MC8qpco%3D',6]"
//        val (msgID, caller, callee, time, url) = mapper.readValue("[2516,'500000002756','',1504082734,'http://oss-cn-hangzhou.aliyuncs.com/jiafeigou-test/500000002756/1504082734.jpg?OSSAccessKeyId=xjBdwD1du8lf2wMI&Expires=1504687544&Signature=zuii8SpBgGRcdiA%2F8xA7MC8qpco%3D',6]", List::class.java)
        val items = response.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        java.lang.Long.parseLong(items[3]);
        println(java.lang.Long.parseLong(items[3]))
    }


}
