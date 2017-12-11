@file:Suppress("MemberVisibilityCanPrivate")

package com.example.yzd.helloworld

import android.util.Log
import com.cylan.jiafeigou.module.AccountBox
import com.cylan.jiafeigou.module.DeviceBox
import com.cylan.jiafeigou.module.PropertyBox
import io.objectbox.Box
import io.objectbox.BoxStore
import java.util.*

/**
 * Created by yzd on 17-12-3.
 */
object DBSupervisor {
    private val TAG = DBSupervisor::class.java.simpleName
    @JvmStatic
    val propertyBox: Box<PropertyBox> = BoxStore.getDefault().boxFor(PropertyBox::class.java)
    @JvmStatic
    val deviceBox: Box<DeviceBox> = BoxStore.getDefault().boxFor(DeviceBox::class.java)
    @JvmStatic
    val accountBox: Box<AccountBox> = BoxStore.getDefault().boxFor(AccountBox::class.java)

    @JvmStatic
    fun getProperty(hash: Long): PropertyBox? {
        Log.d(TAG, "DBSupervisor.getProperty for key:$hash")
        return propertyBox.query().build().findFirst()
    }

    @JvmStatic
    fun putProperty(hash: Long, uuid: String, msgId: Int, version: Long, bytes: ByteArray) {
        Log.d(TAG, "DBSupervisor.putProperty for key:$hash,uuid is:$uuid,msgId is:$msgId,version is:$version,bytes is:${Arrays.toString(bytes)}")
    }

    @JvmStatic
    fun getDevice(uuid: String): DeviceBox? {
        Log.d(TAG, "")
        return null
    }

    @JvmStatic
    fun putDevice(device: DeviceBox) {
        Log.d(TAG, "")
    }

    @JvmStatic
    fun getAllDevices(): List<DeviceBox> {
        Log.d(TAG, "getAllDevices")
        return deviceBox.all
    }

    @JvmStatic
    fun putAllDevices() {
        Log.d(TAG, "")
    }

    fun getAccount(): AccountBox? {
        Log.d(TAG, "getAccount")
        return accountBox.query().build().findFirst()
    }

}