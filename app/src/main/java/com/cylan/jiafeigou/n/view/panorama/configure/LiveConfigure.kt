package com.cylan.jiafeigou.n.view.panorama.configure

import com.google.api.client.util.DateTime
import com.google.api.client.util.Key
import com.google.api.services.youtube.model.LiveBroadcast

/**
 * Created by yanzhendong on 2017/9/9.
 */

class FacebookConfigure


class YoutubeConfigure {
    @Key
    var broadcast: LiveBroadcast? = null

    var account: String? = null
    var title: String? = null
    var description: String? = null
    var startTime: DateTime? = null
    var endTime: DateTime? = null
}


class WeiboConfigure

class RtmpConfigure