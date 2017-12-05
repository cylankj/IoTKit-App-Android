package com.cylan.jiafeigou.module

import android.util.Log
import com.cylan.jiafeigou.support.log.AppLogger
import kotlin.reflect.KProperty

/**
 * Created by yzd on 17-12-2.
 */
object DeviceSupervisor : Supervisor {
    private val TAG = DeviceSupervisor::class.java.simpleName
    private val devices: MutableMap<String, Device> = mutableMapOf()

    init {
        monitorReportDevices()
    }

    private fun monitorReportDevices() {
        AppCallbackSupervisor.observeReportDevice().subscribe(DeviceSupervisor::receiveReportDevices) {
            it.printStackTrace()
            AppLogger.e(it)
            monitorReportDevices()
        }
    }

    private fun receiveReportDevices(event: AppCallbackSupervisor.ReportDeviceEvent) {
        Log.d(TAG, "receive report devices:${event.devices}")
    }


    @JvmStatic
    fun getDevice(uuid: String): Device? {
        var device = devices[uuid]
        if (device == null) {
            Log.d(TAG, "DeviceSupervisor.getDevice:memory cache for device is miss with key:$uuid," +
                    "trying to get from disk. ")
            DBSupervisor.getDevice(uuid)?.apply {
                device = Device(this)
                devices[uuid] = device!!
            }
        }
        Log.d(TAG, "DeviceSupervisor.getDevice:the result of getDevice for key:$uuid is:$device")
        return device
    }

    data class DeviceParameter(var uuid: String, var device: Device) : Supervisor.Parameter

    interface DeviceHooker : Supervisor.Hooker<DeviceParameter>

    class DeviceAction : Supervisor.Action<DeviceParameter> {
        override fun process(parameter: DeviceParameter): DeviceParameter? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    @JvmStatic
    fun <T : DP> getValue(device: Device, property: KProperty<*>): T? {
        val msgId = (property.annotations.first { it is MsgId } as MsgId).msgId
        return PropertySupervisor.getValue(device.box.uuid, msgId)
    }


    @JvmStatic
    fun getDevices(): List<Device> {
        val map = DBSupervisor.getAllDevices().map { Device(it) }
                .associateBy { it.box.uuid }
        devices.putAll(map)
        return devices.values.toList()
    }

    @JvmStatic
    fun addHooker(hooker: DeviceHooker) {
        HookerSupervisor.addHooker(this, DeviceParameter::class.java, hooker)
    }

    @JvmStatic
    fun removeHooker(hooker: DeviceHooker) {
        HookerSupervisor.removeHooker(this, DeviceParameter::class.java, hooker)
    }

}