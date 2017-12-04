@file:Suppress("MemberVisibilityCanPrivate")

package com.example.yzd.helloworld

import android.util.SparseArray
import com.cylan.jiafeigou.module.DPNet
import com.cylan.jiafeigou.module.DPStandby


/**
 * Created by yzd on 17-12-3.
 *
 *
 *
 */
object PropertyTypes {


    private val propertiesTypes = SparseArray<Class<*>>()
    const val NET_201 = 201
    const val MAC_202 = 202
    const val CAMERA_STANDBY_508 = 508

    init {
        propertiesTypes.put(NET_201, DPNet::class.java)
        propertiesTypes.put(MAC_202, Int::class.java)
        propertiesTypes.put(CAMERA_STANDBY_508, DPStandby::class.java)
    }

    @JvmStatic
    fun getType(msgId: Int): Class<*>? = propertiesTypes.get(msgId)
}