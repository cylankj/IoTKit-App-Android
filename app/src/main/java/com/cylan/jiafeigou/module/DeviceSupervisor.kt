package com.cylan.jiafeigou.module

/**
 * Created by yzd on 17-12-2.
 */
object DeviceSupervisor : Supervisor {
//    private val TAG = DeviceSupervisor::class.java.simpleName
//    private val devices: MutableMap<String, Device> = mutableMapOf()
//
//    @JvmStatic
//    fun monitorReportDevices() {
//        val subscribe = AppCallbackSupervisor.observe(Array<JFGDevice>::class.java).subscribe(DeviceSupervisor::receiveReportDevices) {
//            it.printStackTrace()
//            AppLogger.e(it)
//            monitorReportDevices()
//        }
//        SubscriptionSupervisor.subscribe(this, SubscriptionSupervisor.CATEGORY_DEFAULT, "monitorReportDevices", subscribe)
//    }
//
//    private fun receiveReportDevices(event: Array<JFGDevice>) {
//        Log.d(TAG, "receive report devices:$devices")
//        event.map { DeviceBox(it.uuid.toLong(), it.uuid, it.sn, it.alias, it.shareAccount, it.pid, it.vid, it.regionType) }
//                .apply {
//                    DBSupervisor.putDevices(this)
//                }
//    }
//
//
//    @JvmStatic
//    fun getDevice(uuid: String): Device? {
//        var device = devices[uuid]
//        if (device == null) {
//            Log.d(TAG, "DeviceSupervisor.getDevice:memory cache for device is miss with key:$uuid," +
//                    "trying to get from disk. ")
//            DBSupervisor.getDevice(uuid)?.apply {
//                device = Device(this)
//                devices[uuid] = device!!
//            }
//        }
//        Log.d(TAG, "DeviceSupervisor.getDevice:the result of getDevice for key:$uuid is:$device")
//        return device
//    }
//
//    data class DeviceParameter(var uuid: String, var device: Device)
//
//    abstract class DeviceHooker : Supervisor.Hooker {
//        override fun parameterType(): Array<Class<*>> = arrayOf(DeviceParameter::class.java)
//
//        override fun hooker(action: Supervisor.Action) {
//            val parameter = action.parameter()
//            when (parameter) {
//                is DeviceParameter -> doHooker(action, parameter)
//                else -> action.process()
//            }
//        }
//
//        open protected fun doHooker(action: Supervisor.Action, parameter: DeviceParameter) = action.process()
//
//    }
//
//    private data class DeviceAction(private val parameter: DeviceParameter) : Supervisor.Action {
//        override fun process(): Any? {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//
//        override fun parameter() = parameter
//        override fun toString(): String {
//            return "DeviceAction(parameter=$parameter)"
//        }
//
//    }
//
//    @JvmStatic
//    fun <T : DP> getValue(device: Device, property: KProperty<*>): T? {
//        val msgId = (property.annotations.first { it is MsgId } as MsgId).msgId
//        return PropertySupervisor.getValue(device.box.uuid, msgId)
//    }
//
//
//    @JvmStatic
//    fun getDevices(): List<Device> {
//        val map = DBSupervisor.getAllDevices().map { Device(it) }
//                .associateBy { it.box.uuid }
//        devices.putAll(map)
//        return devices.values.toList()
//    }
}