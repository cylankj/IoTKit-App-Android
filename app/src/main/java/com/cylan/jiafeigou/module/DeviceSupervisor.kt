package com.cylan.jiafeigou.module

import android.util.Log
import com.cylan.jiafeigou.support.log.AppLogger
import kotlin.reflect.KProperty

/**
 * Created by yzd on 17-12-2.
 */
object DeviceSupervisor {
    private val TAG = DeviceSupervisor::class.java.simpleName
    private val devices: MutableMap<String, Device> = mutableMapOf()
    private val hookers = mutableListOf<DeviceHooker>()

    interface DeviceHooker {
        fun <T> hook(device: Device, uuid: String, msgId: Int, value: T): T
    }

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

    @JvmStatic
    fun <T> getValue(device: Device, property: KProperty<*>): T {
        val msgId = (property.annotations.first { it is MsgId } as MsgId).msgId
        val value = PropertySupervisor.getValue<DP>(device.box.uuid, msgId)
        val hooker = performHooker(device, device.box.uuid, msgId, value)
        return hooker as T
    }

    private fun <T> performHooker(device: Device, uuid: String, msgId: Int, value: T?): T? {
        var retValue = value
        for (hooker in hookers) {
            retValue = hooker.hook(device, uuid, msgId, value)
            if (retValue != value) {
                break
            }
        }
        Log.d(TAG, "performHooker finished.the hooker result for device:$device,uuid:$uuid,msgId$msgId is:${retValue != value}")
        return retValue
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
        Log.d(TAG, "add hooker:$hooker")
        hookers.add(hooker)
    }

    @JvmStatic
    fun removeHooker(hooker: DeviceHooker) {
        Log.d(TAG, "remove hooker:$hooker")
        hookers.remove(hooker)
    }

}