package com.cylan.jiafeigou.utils

import com.cylan.jiafeigou.server.H
import com.fasterxml.jackson.databind.ObjectMapper
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
    @Test
    fun testT() {
        val mapper = ObjectMapper(MessagePackFactory())


        var header = arrayOf(20000, "www", "ggg", 8476979L, arrayOf("AAA", "BBB"))

        val bytes = mapper.writeValueAsBytes(header)

        var ss= mapper.readValue(bytes, H::class.java)

        print("$ss")


    }


}
