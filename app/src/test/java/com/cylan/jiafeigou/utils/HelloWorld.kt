package com.cylan.jiafeigou.utils

import com.cylan.jiafeigou.server.MIDMessageHeader
import com.cylan.jiafeigou.server.VersionHeader
import com.cylan.jiafeigou.server.VersionValue
import com.cylan.jiafeigou.server.cache.PropertyItem
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

        val (msgId, caller, callee, seq, body) = mapper.readValue(bytes, List::class.java)
        val readValue = mapper.readValue(bytes, List::class.java)
        var (a, b) = body as List<*>

        print("$msgId ,$caller,$callee,$seq,$a , $b")


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

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    data class SSS(var value: Any? = null) : VersionHeader()


    /**只针对 array 类型的数据才需要 cast, 如果是原始类型,比如 int string, 直接 asInt ,asString 就行了**/
    fun <T : Any> cast(propertyItem: PropertyItem, defaultValue: T): T {
        return try {
            objectMapper.get().convertValue(propertyItem.value, defaultValue::class.java).apply { (this as? VersionHeader)?.version = propertyItem.version }
        } catch (e: Exception) {
            println(e.message)
            defaultValue
        }
    }

    @Test
    fun testA() {
        val mapper = jacksonObjectMapper()

        val item = PropertyItem(9, "SSs", 88, 47686, "AAAAA")


        val cast = item.cast(VersionValue())

        println(cast)
    }


}
