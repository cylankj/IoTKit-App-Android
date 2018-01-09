package com.cylan.jiafeigou.module;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;

/**
 * Created by yanzhendong on 2018/1/3.
 */

public class CameraLiveActionHelper {
    public String uuid;
    public volatile boolean isLive = true;
    public volatile boolean isPlaying = false;
    public volatile boolean isLoading = false;
    public volatile boolean isSpeakerOn = false;
    public volatile boolean isMicrophoneOn = false;
    public volatile boolean isLiveSlow = false;
    public volatile boolean isLiveBad = false;
    public volatile boolean isStandBy = false;
    public volatile boolean isSDCardExist = false;
    public volatile boolean isSDCardFormatted = false;
    public volatile DpMsgDefine.DPNet deviceNet;
    public volatile DpMsgDefine.DPTimeZone deviceTimezone;
    public volatile int playCode;
    public volatile long lastPlayTime = 0;
    public volatile int lastReportedPlayError = 0;
    public volatile Bitmap lastLiveThumbPicture;
    public volatile boolean isNetworkConnected = true;
    public volatile boolean isPendingPlayLiveActionCompleted = true;
    public volatile boolean isPendingStopLiveActionCompleted = true;
    public volatile boolean isPendingCaptureActionCompleted = true;
    public volatile boolean isPendingHistoryPlayActionCompleted = true;
    public volatile boolean isPendingPlayLiveActionTimeOutActionReached = false;
    public volatile boolean isLastLiveThumbPictureChanged = true;
    public volatile boolean isVideoResolutionReached;
    public volatile boolean hasPendingResumeToPlayVideoAction = false;

    public CameraLiveActionHelper(String uuid) {
        this.uuid = uuid;
    }

    public void onUpdateDeviceInformation() {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        this.isStandBy = JFGRules.isDeviceStandBy(device);
        this.isSDCardExist = JFGRules.isSDCardExist(device);
        this.deviceNet = device.$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
        this.deviceTimezone = device.$(DpMsgMap.ID_214_DEVICE_TIME_ZONE, new DpMsgDefine.DPTimeZone());
    }

    public void onVideoDisconnected(JFGMsgVideoDisconn videoDisconn) {
        String remote = videoDisconn.remote;
        if (TextUtils.equals(remote, uuid)) {
            this.playCode = videoDisconn.code;
            this.isPendingPlayLiveActionCompleted = true;
            this.isPendingStopLiveActionCompleted = true;
            this.isPendingPlayLiveActionTimeOutActionReached = false;
        }
    }

    public int checkPlayCode(boolean reset) {
        int code = this.playCode;
        if (reset) {
            this.playCode = 0;
        }
        return code;
    }

    public boolean checkPlayTimeout(boolean reset) {
        boolean timeout = this.isPendingPlayLiveActionTimeOutActionReached;
        if (reset) {
            this.isPendingPlayLiveActionTimeOutActionReached = false;
        }
        return timeout;
    }


    public void onVideoPlayStarted(boolean live) {
        this.isPlaying = true;
        this.isLoading = true;
        this.isLiveBad = false;
        this.isLiveSlow = false;
        this.isPendingPlayLiveActionCompleted = false;
        this.isPendingStopLiveActionCompleted = true;
        this.isPendingPlayLiveActionTimeOutActionReached = false;
        this.isVideoResolutionReached = false;
        this.isLive = live;
        this.hasPendingResumeToPlayVideoAction = false;
    }

    public boolean onUpdateVideoSlowState(boolean slow) {
        final boolean isLiveSlow = this.isLiveSlow;
        this.isLiveSlow = slow;
        this.isLoading = slow && this.isPlaying;
        return isLiveSlow;
    }

    public boolean onUpdateVideoFrameFailed() {
        final boolean isLiveBad = this.isLiveBad;
        this.isLiveBad = true;
        this.isLoading = this.isPlaying;
        return isLiveBad;
    }

    public boolean checkLiveLowFrameState(boolean reset) {
        boolean isLow = this.isLiveSlow;
        if (reset) {
            this.isLiveSlow = false;
        }
        return isLow;
    }

    public boolean checkLiveBadFrameState(boolean reset) {
        boolean isBadFrame = this.isLiveBad;
        if (reset) {
            this.isLiveBad = false;
        }
        return isBadFrame;
    }

    public void onVideoResolutionReached(JFGMsgVideoResolution jfgMsgVideoResolution) {
        if (TextUtils.equals(uuid, jfgMsgVideoResolution.peer)) {
            this.isPendingPlayLiveActionCompleted = true;
            this.isPendingStopLiveActionCompleted = true;
            this.isPendingPlayLiveActionTimeOutActionReached = false;
            this.isVideoResolutionReached = true;
            this.isLiveBad = false;
            this.isLiveSlow = false;
            this.isLoading = false;
            this.isPlaying = true;
        }
    }

    public void onVideoPlayStopped(boolean live) {
        this.isPlaying = false;
        this.isLoading = false;
        this.isLiveBad = false;
        this.isLiveSlow = false;
        this.isPendingStopLiveActionCompleted = true;
        this.isPendingPlayLiveActionCompleted = true;
        this.isPendingPlayLiveActionTimeOutActionReached = false;
        this.isVideoResolutionReached = false;
        this.isLive = live;
    }

    public void onVideoPlayTimeOutReached() {
        this.isPendingPlayLiveActionTimeOutActionReached = true;
        this.isPendingStopLiveActionCompleted = true;
        this.isPendingPlayLiveActionCompleted = true;
        this.isLiveBad = false;
        this.isLiveSlow = false;
        this.isPlaying = false;

    }

    public boolean isLoadingFailed() {
        return this.isLiveBad && this.isLiveSlow;
    }

    public void onUpdateHistoryVideoError(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {
        this.playCode = jfgHistoryVideoErrorInfo.code;
    }

    public boolean onUpdatePendingPlayLiveActionCompleted() {
        final boolean isPendingPlayLiveActionCompleted = this.isPendingPlayLiveActionCompleted;
        this.isPendingPlayLiveActionCompleted = true;
        this.isLoading = false;
        return isPendingPlayLiveActionCompleted;
    }

    public boolean onUpdateNetWorkChangedAction(boolean networkConnected) {
        final boolean isNetworkConnected = this.isNetworkConnected;
        this.isNetworkConnected = networkConnected;
        if (!networkConnected) {
            this.isPendingHistoryPlayActionCompleted = true;
            this.isPendingPlayLiveActionTimeOutActionReached = false;
            this.isPendingStopLiveActionCompleted = true;
            this.isLiveBad = false;
            this.isLiveSlow = false;
            this.isPlaying = false;
            this.isLoading = false;
        }
        return isNetworkConnected;
    }

    public void onUpdateLastLiveThumbPicture(CameraLiveActionHelper helper, Bitmap bitmap) {
        helper.lastLiveThumbPicture = bitmap;
        helper.isLastLiveThumbPictureChanged = true;
    }

    public boolean onUpdateStandBy(boolean standBy) {
        final boolean isStandBy = this.isStandBy;
        this.isStandBy = standBy;
        this.hasPendingResumeToPlayVideoAction = CameraLiveHelper.isVideoPlaying(this);
        return isStandBy;
    }

    public boolean onUpdateSDCardFormatted(Integer formatted) {
        final boolean isSDCardFormatted = this.isSDCardFormatted;
        this.isSDCardFormatted = true;
        return isSDCardFormatted;
    }

    public boolean onUpdateSDCardStatus(DpMsgDefine.DPSdcardSummary sdStatus) {
        final boolean isSDCardExist = this.isSDCardExist;
        this.isSDCardExist = JFGRules.hasSdcard(sdStatus);
        return isSDCardExist;
    }

    public boolean onUpdateLive(boolean live) {
        final boolean isLive = this.isLive;
        this.isLive = live;
        return isLive;
    }

    public boolean onUpdatePendingHistoryPlayActionCompleted() {
        final boolean isPendingHistoryPlayActionCompleted = this.isPendingHistoryPlayActionCompleted;
        this.isPendingHistoryPlayActionCompleted = true;
        return isPendingHistoryPlayActionCompleted;
    }
}
