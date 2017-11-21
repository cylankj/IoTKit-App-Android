package com.cylan.jiafeigou.module.message

import org.msgpack.packer.Packer
import org.msgpack.template.AbstractTemplate
import org.msgpack.unpacker.Unpacker
import java.io.IOException

/**
 * Created by yanzhendong on 2017/11/13.
 */
class DPListConverter : AbstractTemplate<DPList>() {

    @Throws(IOException::class)
    override fun write(packer: Packer, dpMessages: DPList?, b: Boolean) {
        if (dpMessages == null) return

        packer.writeArrayBegin(dpMessages.size)
        for (message in dpMessages) {
            packer.writeArrayBegin(3)
            packer.write(message.msgId)
            packer.write(message.version)
            packer.write(message.value)
            packer.writeArrayEnd()
        }
        packer.writeArrayEnd()
    }

    @Throws(IOException::class)
    override fun read(unpacker: Unpacker, dpMessages: DPList, b: Boolean): DPList {
        val count = unpacker.readArrayBegin()
        val result = DPList()
        for (i in 0 until count) {
            unpacker.readArrayBegin()
            val msgId = unpacker.readInt()
            val version = unpacker.readLong()
            val bytes = unpacker.readByteArray()
            unpacker.readArrayEnd()
            val dpMessage = DPMessage(msgId, version, bytes)
            result.add(dpMessage)
        }
        unpacker.readArrayEnd()
        return result
    }
}