package com.cylan.jiafeigou.module.message

import android.os.Parcel
import android.os.Parcelable
import org.msgpack.annotation.Ignore
import org.msgpack.annotation.Index
import org.msgpack.annotation.Message
import java.io.Serializable
import java.util.*

/**
 * Created by yanzhendong on 2017/11/13.
 * 实验阶段,暂勿使用
 */
@Message
open class MIDHeader(
        @Index(0) var msgId: Int = 0,
        @Index(1) var caller: String = "",
        @Index(2) var callee: String = "",
        @Index(3) var seq: Long = 0L
) : Parcelable, Serializable {
    //不能为空
    @Ignore lateinit var rawBytes: ByteArray

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(msgId)
        writeString(caller)
        writeString(callee)
        writeLong(seq)
    }

    override fun toString(): String {
        return "MIDHeader(msgId=$msgId, caller='$caller', callee='$callee', seq=$seq)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MIDHeader) return false

        if (msgId != other.msgId) return false
        if (caller != other.caller) return false
        if (callee != other.callee) return false
        if (seq != other.seq) return false

        return true
    }

    override fun hashCode(): Int {
        var result = msgId
        result = 31 * result + caller.hashCode()
        result = 31 * result + callee.hashCode()
        result = 31 * result + seq.hashCode()
        return result
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MIDHeader> = object : Parcelable.Creator<MIDHeader> {
            override fun createFromParcel(source: Parcel): MIDHeader = MIDHeader(source)
            override fun newArray(size: Int): Array<MIDHeader?> = arrayOfNulls(size)
        }
    }
}

@Message
open class DPMessage(
        @Index(0) var msgId: Int = 0,
        @Index(1) var version: Long = 0L,
        @Index(2) var value: ByteArray = byteArrayOf()
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DPMessage) return false

        if (msgId != other.msgId) return false
        if (version != other.version) return false
        if (!Arrays.equals(value, other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = msgId
        result = 31 * result + version.hashCode()
        result = 31 * result + Arrays.hashCode(value)
        return result
    }

    override fun toString(): String {
        return "DPMessage(msgId=$msgId, version=$version, value=${Arrays.toString(value)})"
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readLong(),
            source.createByteArray()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(msgId)
        writeLong(version)
        writeByteArray(value)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DPMessage> = object : Parcelable.Creator<DPMessage> {
            override fun createFromParcel(source: Parcel): DPMessage = DPMessage(source)
            override fun newArray(size: Int): Array<DPMessage?> = arrayOfNulls(size)
        }
    }

}

class DPList : ArrayList<DPMessage>()

