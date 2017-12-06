@file:Suppress("MemberVisibilityCanPrivate")

package com.cylan.jiafeigou.module

import android.util.Log
import io.objectbox.Box
import io.objectbox.BoxStore

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
    fun putProperty(property: PropertyBox) {
        propertyBox.put(property)
    }

    @JvmStatic
    fun getDevice(uuid: String): DeviceBox? {
        Log.d(TAG, "getDevice:$uuid")
        return null
    }

    @JvmStatic
    fun putDevice(device: DeviceBox) {
        Log.d(TAG, "putDevice:$device")
        deviceBox.put(device)
    }

    @JvmStatic
    fun getAllDevices(): List<DeviceBox> {
        Log.d(TAG, "getAllDevices")
        return deviceBox.all
    }

    @JvmStatic
    fun putDevices(devices: Collection<DeviceBox>) {
        Log.d(TAG, "")
        deviceBox.put(devices)
    }

    @JvmStatic
    fun getAccount(): AccountBox? {
        Log.d(TAG, "getAccount")
        return accountBox.query().build().findFirst()
    }

    @JvmStatic
    fun saveAccount(account: AccountBox) {
        Log.d(TAG, "saveAccount:$account")
        accountBox.put(account)
    }

}