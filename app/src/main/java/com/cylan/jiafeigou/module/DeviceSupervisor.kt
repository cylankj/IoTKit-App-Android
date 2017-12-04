package com.cylan.jiafeigou.module

import android.util.Log
import com.cylan.jiafeigou.support.log.AppLogger
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.jvmErasure

/**
 * Created by yzd on 17-12-2.
 */
object DeviceSupervisor {
    private val TAG = DeviceSupervisor::class.java.simpleName
    private val devices: MutableMap<String, Device> = mutableMapOf()
    private val hookers = mutableListOf<DeviceHooker>()

    interface DeviceHooker {
        fun hook(device: Device, uuid: String, msgId: Int): Boolean
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
        if (!performHooker(device, device.box.uuid, msgId)) {
            val value = PropertySupervisor.getValue<DP>(device.box.uuid, msgId)
            return if (property.returnType.jvmErasure == value::class) {
                value as T
            } else {
                throw ClassCastException("getProperty from Device(uuid=${device.box.uuid}) for property id:$msgId," +
                        "property name:${property.name},property type:${property.returnType} failed." +
                        "value:$value cannot cast to ${property.returnType}")
            }
        } else {
            throw IllegalAccessException("cannot get value for device:$device,uuid:${device.box.uuid},msgId:$msgId,it is hooked," +
                    "make sure you have the permission")
        }
    }

    private fun performHooker(device: Device, uuid: String, msgId: Int): Boolean {
        val hooked = hookers.any { it.hook(device, uuid, msgId) }
        Log.d(TAG, "performHooker finished.the hooker result for device:$device,uuid:$uuid,msgId$msgId is:$hooked")
        return hooked
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