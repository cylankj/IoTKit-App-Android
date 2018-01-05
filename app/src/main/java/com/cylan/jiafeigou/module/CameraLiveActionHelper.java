package com.cylan.jiafeigou.module;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;

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
    public volatile int playCode;
    public volatile long lastPlayTime = 0;
    public volatile int lastPlayError = 0;
    public volatile Bitmap lastLiveThumbPicture;
    public volatile boolean isNetworkConnected = true;
    public volatile boolean isPendingPlayLiveActionCompleted = true;
    public volatile boolean isPendingStopLiveActionCompleted = true;
    public volatile boolean isPendingCaptureActionCompleted = true;
    public volatile boolean isPendingPlayLiveActionTimeOutActionReached = false;

    public CameraLiveActionHelper(String uuid) {
        this.uuid = uuid;
    }

    public void onVideoDisconnected(JFGMsgVideoDisconn videoDisconn) {
        String remote = videoDisconn.remote;
        if (TextUtils.equals(remote, uuid)) {
            playCode = videoDisconn.code;
            isPendingPlayLiveActionCompleted = true;
            isPendingPlayLiveActionTimeOutActionReached = false;
        }
    }

    public int checkPlayCode(boolean reset) {
        int code = playCode;
        if (reset) {
            playCode = 0;
        }
        return code;
    }

    public boolean checkPlayTimeout(boolean reset) {
        boolean timeout = isPendingPlayLiveActionTimeOutActionReached;
        if (reset) {
            isPendingPlayLiveActionTimeOutActionReached = false;
        }
        return timeout;
    }


    public void onVideoPlayStarted(boolean live) {
        this.isPlaying = true;
        this.isLoading = true;
        this.isLiveBad = false;
        this.isLiveSlow = false;
        this.isPendingPlayLiveActionCompleted = false;
        this.isPendingPlayLiveActionTimeOutActionReached = false;
        this.isLive = live;
    }

    public void onUpdateVideoSlowState(boolean slow) {
        isLiveSlow = slow;
        isLoading = slow && isPlaying;
    }

    public void onUpdateVideoFrameFailed() {
        isLiveBad = true;
        isLoading = isPlaying;
    }

    public boolean checkLiveLowFrameState(boolean reset) {
        boolean isLow = isLiveSlow;
        if (reset) {
            isLiveSlow = false;
        }
        return isLow;
    }

    public boolean checkLiveBadFrameState(boolean reset) {
        boolean isBadFrame = isLiveBad;
        if (reset) {
            isLiveBad = false;
        }
        return isBadFrame;
    }

    public void onVideoResolutionReached(JFGMsgVideoResolution jfgMsgVideoResolution) {
        if (TextUtils.equals(uuid, jfgMsgVideoResolution.peer)) {
            isPendingPlayLiveActionCompleted = true;
            isLiveBad = false;
            isLiveSlow = false;
            isLoading = false;
            isPlaying = true;
        }
    }

    public void onVideoPlayStopped(boolean live) {
        this.isPlaying = false;
        this.isLoading = false;
        this.isLiveBad = false;
        this.isLiveSlow = false;
        this.isPendingStopLiveActionCompleted = true;
        this.isPendingPlayLiveActionCompleted = true;
        this.isLive = live;
    }

    public void onUpdateVideoPlayTimeOutAction() {
        this.isLiveBad = true;
        this.isLiveSlow = true;
        this.isPendingPlayLiveActionTimeOutActionReached = true;
        this.isPlaying = false;

    }

    public boolean isLoadingFailed() {
        return isLiveBad && isLiveSlow;
    }

    public void onUpdateHistoryVideoError(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {
        this.playCode = jfgHistoryVideoErrorInfo.code;
    }

}
