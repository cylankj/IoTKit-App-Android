package com.cylan.jiafeigou.server.cache

import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.entity.jniCall.JFGDPValue
import com.cylan.jiafeigou.n.base.BaseApplication
import com.fasterxml.jackson.databind.ObjectMapper
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.converter.PropertyConverter
import io.objectbox.query.Query
import org.greenrobot.essentials.hash.FNV64
import org.msgpack.jackson.dataformat.MessagePackFactory
import java.nio.charset.Charset

/**
 * Created by yanzhendong on 2017/8/21.
 */


class PropertyItemConverter : PropertyConverter<Any, ByteArray> {
    override fun convertToDatabaseValue(value: Any?): ByteArray = when (value) {
        is ByteArray -> value
        else -> objectMapper.get().writeValueAsBytes(value)
    }


    override fun convertToEntityProperty(byteArray: ByteArray?): Any? = try {
        objectMapper.get().readValue(byteArray, Any::class.java)
    } catch (e: Exception) {
        0
    }


}

val fnv64: ThreadLocal<FNV64> = object : ThreadLocal<FNV64>() {
    override fun initialValue() = FNV64()
}

fun String.longHash(): Long {
    val bytes = toByteArray(Charset.defaultCharset())
    val fnV64 = fnv64.get()
    fnV64.reset()
    fnV64.update(bytes, 0, bytes.size)
    return fnV64.value
}

fun msgIdKey(uuid: String?, msgId: Int) = "${uuid ?: ""}:$msgId".longHash()

fun versionKey(uuid: String?, msgId: Int, version: Long) = "$uuid:$msgId:$version".longHash()


var objectMapper: ThreadLocal<ObjectMapper> = object : ThreadLocal<ObjectMapper>() {

    override fun initialValue(): ObjectMapper {
        val mapper = ObjectMapper(MessagePackFactory())

        return mapper
    }

}

@Entity
class PropertyItem(@Id(assignable = true)
                   var hash: Long = 0,
                   var uuid: String? = "",
                   var msgId: Int = 0,
                   var version: Long = 0,
                   @Convert(dbType = ByteArray::class, converter = PropertyItemConverter::class) var value: Any? = null) {
    // hash 对于单值属性为 uuid:msgId 的 FNV64 值,对于多值属性为 uuid:msgId:version 的 FNV64的值


    /**---------------------------------For List Body------------------------------------------------**/
    fun asByte(index: Int, defaultValue: Byte) = (this.value as? List<*>)?.getOrNull(index) as? Byte ?: defaultValue

    fun asByteArray(index: Int, defaultValue: ByteArray) = (this.value as? List<*>)?.getOrNull(index) as? ByteArray ?: defaultValue

    fun asShort(index: Int, defaultValue: Short) = (this.value as? List<*>)?.getOrNull(index) as? Short ?: defaultValue

    fun asInt(index: Int, defaultValue: Int) = (this.value as? List<*>)?.getOrNull(index) as? Int ?: defaultValue

    fun asLong(index: Int, defaultValue: Long) = (this.value as? List<*>)?.getOrNull(index) as? Long ?: defaultValue

    fun asString(index: Int, defaultValue: String) = (this.value as? List<*>)?.getOrNull(index) as? String ?: defaultValue

    fun asBoolean(index: Int, defaultValue: Boolean) = (this.value as? List<*>)?.getOrNull(index) as? Boolean ?: defaultValue

    fun asFloat(index: Int, defaultValue: Float) = (this.value as? List<*>)?.getOrNull(index) as? Float ?: defaultValue

    fun asList(index: Int, defaultValue: MutableList<*>) = (this.value as? List<*>)?.getOrNull(index) as? MutableList<*> ?: defaultValue

    fun asMap(index: Int, defaultValue: MutableMap<*, *>) = (this.value as? List<*>)?.getOrNull(index) as? MutableMap<*, *> ?: defaultValue
    /**---------------------------------------------------------------------------------------------**/

    /**-----------------------For Primary Body------------------------------------------------------**/
    fun asByte(defaultValue: Byte) = this.value as? Byte ?: defaultValue

    fun asByteArray(defaultValue: ByteArray) = this.value  as? ByteArray ?: defaultValue

    fun asShort(defaultValue: Short) = this.value  as? Short ?: defaultValue

    fun asInt(defaultValue: Int) = this.value  as? Int ?: defaultValue

    fun asLong(defaultValue: Long) = this.value  as? Long ?: defaultValue

    fun asString(defaultValue: String) = this.value as? String ?: defaultValue

    fun asBoolean(defaultValue: Boolean) = this.value  as? Boolean ?: defaultValue

    fun asFloat(defaultValue: Float) = this.value  as? Float ?: defaultValue

    fun asList(defaultValue: MutableList<*>) = this.value  as? MutableList<*> ?: defaultValue

    fun asMap(defaultValue: MutableMap<*, *>) = this.value as? MutableMap<*, *> ?: defaultValue
    /**----------------------------------------------------------------------------------------------**/


}

var EMPTY_PROPERTY_ITEM: PropertyItem = PropertyItem()

var emptyPropertyItemList: MutableList<PropertyItem> = mutableListOf()

fun getProperty(uuid: String? = "", msgId: Int): PropertyItem = BaseApplication.getPropertyItemBox()[msgIdKey(uuid, msgId)] ?: EMPTY_PROPERTY_ITEM

fun getPropertyQuery(uuid: String? = "", msgId: Int): Query<PropertyItem> = BaseApplication.getPropertyItemBox().query().equal(PropertyItem_.__ID_PROPERTY, msgIdKey(uuid, msgId)).build()

fun getPropertyList(uuid: String? = "", msgId: Int): MutableList<PropertyItem> = BaseApplication.getPropertyItemBox().query().equal(PropertyItem_.uuid, uuid ?: "").equal(PropertyItem_.msgId, msgId.toLong()).build().find()

fun getPropertyListQuery(uuid: String? = "", msgId: Int): Query<PropertyItem> = BaseApplication.getPropertyItemBox().query().equal(PropertyItem_.uuid, uuid ?: "").equal(PropertyItem_.msgId, msgId.toLong()).build()

fun saveProperty(uuid: String? = "", valueMap: MutableMap<Int, List<*>>?, hashStrategy: ((String?, Int, Long) -> Long?)?) {

    var items: MutableList<PropertyItem> = mutableListOf()

    valueMap?.forEach { item ->
        item.value.forEach {
            var msg = it as? JFGDPMsg
            val propertyItem = PropertyItem(hashStrategy?.invoke(uuid, item.key, msg?.version ?: 0) ?: msgIdKey(uuid, item.key), uuid, item.key, msg?.version ?: 0, msg?.packValue)
            items.add(propertyItem)
        }
    }?.apply { BaseApplication.getPropertyItemBox().put(items) }
}

fun saveProperty(uuid: String? = "", valueList: MutableList<*>?, hashStrategy: ((String?, Int, Long) -> Long?)?) {
    val items: MutableList<PropertyItem> = mutableListOf()
    valueList?.forEach {
        var msg = it as? JFGDPMsg
        val msdId = msg?.id?.toInt() ?: 0
        val version = msg?.version ?: 0
        val item = PropertyItem(hashStrategy?.invoke(uuid, msdId, version) ?: msgIdKey(uuid, msdId), uuid, msdId, msg?.version ?: 0, msg?.packValue)
        items.add(item)
    }?.apply { BaseApplication.getPropertyItemBox().put(items) }
}

fun saveProperty(maps: Map<String, Map<Long, *>>, hashStrategy: ((String?, Int, Long) -> Long?)?) {

    val items: MutableList<PropertyItem> = mutableListOf()

    maps.forEach { cidItem ->

        cidItem.value.forEach { msgItem ->

            when (msgItem.value) {

                is Array<*> -> {
                    (msgItem.value as? Array<*>)?.forEach {
                        val dp = it as? JFGDPValue
                        val version = dp?.version ?: 0
                        val value = dp?.value ?: byteArrayOf()
                        val item = PropertyItem(hashStrategy?.invoke(cidItem.key, msgItem.key.toInt(), version) ?: msgIdKey(cidItem.key, msgItem.key.toInt()), cidItem.key, msgItem.key.toInt(), version, value)
                        items.add(item)
                    }
                }

            }


        }
    }.apply { BaseApplication.getPropertyItemBox().put(items) }


}

//fun saveProperty(Map<String, Map<Long, List<JFGDPValue>>>)

object HashStrategyFactory {

    fun select(uuid: String?, msgId: Int, version: Long): Long {
        return when (msgId) {
            in intArrayOf(701, 602, 222) -> versionKey(uuid, msgId, version)
            else -> msgIdKey(uuid, msgId)
        }
    }


}

/**
 * string,   cid
string,   sn
string,   alias
string,   shareAccount
int,      pid //原os
int,      regionType  //详见 DPIDCloudStorage
string,   vid
 * */
@Entity
data class Device(@Id(assignable = true) var uuid: Long = 0, var sn: String?, var alias: String?, var shareAccount: String? = "", var pid: Int? = 0, var regionType: Int? = 0, var vid: String?)