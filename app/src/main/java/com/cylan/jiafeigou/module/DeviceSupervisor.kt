package com.example.yzd.helloworld

import android.util.Log
import com.cylan.jiafeigou.module.Device

/**
 * Created by yzd on 17-12-2.
 */
object DeviceSupervisor {
    private val TAG = DeviceSupervisor::class.java.simpleName
    private val devices: MutableMap<String, Device> = mutableMapOf()
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
    fun getDevices(): List<Device> {
        val map = DBSupervisor.getAllDevices().map { Device(it) }
                .associateBy { it.box.uuid }
        devices.putAll(map)
        return devices.values.toList()
    }

}