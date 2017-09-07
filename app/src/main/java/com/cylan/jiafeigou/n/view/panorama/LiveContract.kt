package com.cylan.jiafeigou.n.view.panorama

import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.rtmp.youtube.util.EventData
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException

/**
 * Created by yanzhendong on 2017/9/7.
 */


interface YouTubeLiveSetting {
    interface View : JFGView {
        fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int)
        fun onUserRecoverableAuthIOException(error: UserRecoverableAuthIOException)
        fun onLiveEventResponse(rtmp: EventData)
    }

    interface Presenter : JFGPresenter<View> {

        fun getLiveList(credential: GoogleAccountCredential, liveBroadcastID: String)

        fun getLiveFromDevice()
    }

}