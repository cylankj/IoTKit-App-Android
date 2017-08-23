package com.cylan.jiafeigou.server

import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.jiafeigou.server.cache.saveProperty

/**
 * Created by yanzhendong on 2017/8/21.
 * 过渡性使用,将来会废弃
 */


fun saveRobotSyncData(fromDevice: Boolean, uuid: String? = "", msgs: MutableList<JFGDPMsg>?) {
    saveProperty(uuid, msgs, null)
}

fun getPageMessage(uuid: String?, page: PAGE_MESSAGE) {
//    page.message

}

var header: Array<Any>? = null
