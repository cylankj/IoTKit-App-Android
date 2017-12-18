package com.cylan.jiafeigou.server.cache

//import com.cylan.jiafeigou.server.VersionHeader

/**
 * Created by yanzhendong on 2017/8/21.
 */


//class PropertyItemConverter : PropertyConverter<Any, ByteArray> {
//    override fun convertToDatabaseValue(value: Any?): ByteArray = try {
//        when (value) {
//            is ByteArray -> value
//            else -> objectMapper.get().writeValueAsBytes(value)
//        }
//    } catch (e: Exception) {
//        byteArrayOf()
//    }
//
//    override fun convertToEntityProperty(byteArray: ByteArray?): Any? = try {
//        objectMapper.get().readValue(byteArray, Any::class.java)
//    } catch (e: Exception) {
//        0
//    }
//
//}

//val fnv64: ThreadLocal<FNV64> = object : ThreadLocal<FNV64>() {
//    override fun initialValue() = FNV64()
//}

fun String.longHash(): Long {
//    val bytes = toByteArray(Charset.defaultCharset())
//    val fnV64 = fnv64.get()
//    fnV64.reset()
//    fnV64.update(bytes, 0, bytes.size)
    return hashCode().toLong()
}

//val APPLICATION_NOTICE = "THIS APPLICATION ONLY SUPPORT SINGLE ACCOUNT"
//
//fun msgIdKey(uuid: String?, msgId: Long) = "$uuid:$msgId".longHash()
//
//fun versionKey(uuid: String?, msgId: Long, version: Long) = "$uuid:$msgId:$version".longHash()

//fun deviceKey(uuid: String?) = "$APPLICATION_NOTICE,THE DEVICE IS:$uuid".longHash()

//fun accountKey() = APPLICATION_NOTICE.longHash()


//var objectMapper: ThreadLocal<ObjectMapper> = object : ThreadLocal<ObjectMapper>() {
//
//    override fun initialValue() = ObjectMapper(MessagePackFactory())
//
//}

//@Entity
//data class KeyValueStringItem(@Id(assignable = true) var key: Long, var value: String)
//
//@Entity
//@JsonFormat(shape = JsonFormat.Shape.ARRAY)
//class PropertyItem(@Id(assignable = true)
//                   @JsonIgnore var hash: Long = 0,
//                   @JsonIgnore var uuid: String? = "",
//                   var msgId: Int = 0,
//                   var version: Long = 0,
//                   @Convert(dbType = ByteArray::class, converter = PropertyItemConverter::class) var value: Any? = null
//) {
//
//    @Suppress("UNCHECKED_CAST")
//    fun <T : Any> cast(defaultValue: T): T = try {
//        when (defaultValue) {
////            is VersionHeader -> objectMapper.get().convertValue(value, defaultValue::class.java).apply { (this as VersionHeader).version = version }
//            is DpMsgDefine.DPPrimary<*> -> DpMsgDefine.DPPrimary(value)
//            is BaseDataPoint -> objectMapper.get().convertValue(value, defaultValue::class.java).apply { val v = (this as BaseDataPoint);v.version = version;v.msgId = msgId }
//            else -> if (defaultValue::class.java.isInstance(value)) value else defaultValue
//        } as T
//    } catch (e: Exception) {
//        Log.e(JConstant.CYLAN_TAG, e.message)
//        defaultValue
//    }
//
//    fun <T : Any> cast(clz: Class<T>): T? = try {
//        objectMapper.get().convertValue(value, clz)
//    } catch (e: Exception) {
//        null
//    }
//
//// hash 对于单值属性为 uuid:msgId 的 FNV64 值,对于多值属性为 uuid:msgId:version 的 FNV64的值
//    /**---------------------------------For List Body------------------------------------------------**/
//    fun asByte(index: Int, defaultValue: Byte) = (this.value as? List<*>)?.getOrNull(index) as? Byte ?: defaultValue
//
//    fun asByteArray(index: Int, defaultValue: ByteArray) = (this.value as? List<*>)?.getOrNull(index) as? ByteArray ?: defaultValue
//
//    fun asShort(index: Int, defaultValue: Short) = (this.value as? List<*>)?.getOrNull(index) as? Short ?: defaultValue
//
//    fun asInt(index: Int, defaultValue: Int) = (this.value as? List<*>)?.getOrNull(index) as? Int ?: defaultValue
//
//    fun asLong(index: Int, defaultValue: Long) = (this.value as? List<*>)?.getOrNull(index) as? Long ?: defaultValue
//
//    fun asString(index: Int, defaultValue: String) = (this.value as? List<*>)?.getOrNull(index) as? String ?: defaultValue
//
//    fun asBoolean(index: Int, defaultValue: Boolean) = (this.value as? List<*>)?.getOrNull(index) as? Boolean ?: defaultValue
//
//    fun asFloat(index: Int, defaultValue: Float) = (this.value as? List<*>)?.getOrNull(index) as? Float ?: defaultValue
//
//    fun asList(index: Int, defaultValue: MutableList<*>) = (this.value as? List<*>)?.getOrNull(index) as? MutableList<*> ?: defaultValue
//
//    fun asMap(index: Int, defaultValue: MutableMap<*, *>) = (this.value as? List<*>)?.getOrNull(index) as? MutableMap<*, *> ?: defaultValue
//    /**---------------------------------------------------------------------------------------------**/
//
//    /**-----------------------For Primary Body------------------------------------------------------**/
//    fun asByte(defaultValue: Byte) = this.value as? Byte ?: defaultValue
//
//    fun asByteArray(defaultValue: ByteArray) = this.value  as? ByteArray ?: defaultValue
//
//    fun asShort(defaultValue: Short) = this.value  as? Short ?: defaultValue
//
//    fun asInt(defaultValue: Int) = this.value  as? Int ?: defaultValue
//
//    fun asLong(defaultValue: Long) = this.value  as? Long ?: defaultValue
//
//    fun asString(defaultValue: String) = this.value as? String ?: defaultValue
//
//    fun asBoolean(defaultValue: Boolean) = this.value  as? Boolean ?: defaultValue
//
//    fun asFloat(defaultValue: Float) = this.value  as? Float ?: defaultValue
//
//    fun asList(defaultValue: MutableList<*>) = this.value  as? MutableList<*> ?: defaultValue
//
//    fun asMap(defaultValue: MutableMap<*, *>) = this.value as? MutableMap<*, *> ?: defaultValue
//    /**----------------------------------------------------------------------------------------------**/
//
//
//}

//var EMPTY_PROPERTY_ITEM: PropertyItem = PropertyItem()
//
//var emptyPropertyItemList: MutableList<PropertyItem> = mutableListOf()
//
//fun getProperty(uuid: String? = "", msgId: Long): PropertyItem = BaseApplication.getPropertyItemBox()[msgIdKey(uuid, msgId)] ?: EMPTY_PROPERTY_ITEM
//
////fun getPropertyQuery(uuid: String? = "", msgId: Long): Query<PropertyItem> = BaseApplication.getPropertyItemBox().query().equal(PropertyItem_.__ID_PROPERTY, msgIdKey(uuid, msgId)).build()
//
////fun getPropertyList(uuid: String? = "", msgId: Long): MutableList<PropertyItem> = BaseApplication.getPropertyItemBox().query().equal(PropertyItem_.uuid, uuid ?: "").equal(PropertyItem_.msgId, msgId.toLong()).build().find()
//
////fun getPropertyListQuery(uuid: String? = "", msgId: Long): Query<PropertyItem> = BaseApplication.getPropertyItemBox().query().equal(PropertyItem_.uuid, uuid ?: "").equal(PropertyItem_.msgId, msgId.toLong()).build()
//
//fun saveProperty(uuid: String? = "", valueMap: MutableMap<Long, List<*>>?, hashStrategy: ((String?, Long, Long) -> Long?)?) = try {
//    {
//    }
//} catch (e: Exception) {
//    Log.i(JConstant.CYLAN_TAG, e.message)
//}

//fun saveProperty(uuid: String? = "", valueList: MutableList<*>?, hashStrategy: ((String?, Long, Long) -> Long?)?) = try {
//} catch (e: Exception) {
//    Log.i(JConstant.CYLAN_TAG, e.message)
//}
//
//fun saveDevices(devices: Array<JFGDevice>) = try {
//    {
//    }
//} catch (e: Exception) {
//    Log.i(JConstant.CYLAN_TAG, e.message)
//}
//
////fun saveProperty(Map<String, Map<Long, List<JFGDPValue>>>)
//
//object HashStrategyFactory {
//
//    fun select(uuid: String?, msgId: Long, version: Long): Long {
//        return when (msgId) {
//            in longArrayOf(701, 602, 512, 511, 505, 401, 222) -> versionKey(uuid, msgId, version)
//            else -> msgIdKey(uuid, msgId)
//        }
//    }
//
//
//}
//@Entity
//data class Device(@Id(assignable = true) var uuid: Long,
//                  var sn: String?,
//                  var alias: String?,
//                  var shareAccount: String? = "",
//                  var pid: Int? = 0,
//                  var regionType: Int? = 0,
//                  var vid: String?) {
//
//    fun cast(): com.cylan.jiafeigou.cache.db.module.Device? {
//        return com.cylan.jiafeigou.cache.db.module.Device(null, uuid.toString(),
//                sn, alias, shareAccount, pid ?: 0, vid, null, regionType ?: 0, null, null, null, null)
//    }
//
//}
//@Entity
//data class Account(@Id(assignable = true) var hash: Long,
//                   var account: String,
//                   var password: String,
//                   var loginType: Int
//)
//
//fun <V : Any> cast(propertyItem: PropertyItem?, defaultValue: V): V {
//    val mapper = jacksonObjectMapper()
//
//    return try {
//        mapper.convertValue(propertyItem?.value, defaultValue::class.java)
//    } catch (e: Exception) {
//        Log.e(JConstant.CYLAN_TAG, "解析出错:${e.message},数据为:${propertyItem?.value}")
//        defaultValue
//    } ?: defaultValue
//
//}