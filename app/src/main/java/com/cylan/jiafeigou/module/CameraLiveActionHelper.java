package com.cylan.jiafeigou.module;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGMsgVideoDisconn;

/**
 * Created by yanzhendong on 2018/1/3.
 */

public class CameraLiveActionHelper {
    private String uuid;
    private volatile boolean isLive = true;
    private volatile boolean isPlaying = false;
    private volatile boolean isLoading = false;
    private volatile boolean isSpeakerOn = false;
    private volatile boolean isMicrophoneOn = false;
    private volatile boolean isLiveSlow = false;
    private volatile boolean isLiveBad = false;
    private volatile boolean isStandBy = false;
    private volatile boolean isSDCardExist = false;
    private volatile int playCode;
    private volatile long lastPlayTime = 0;

    public String getUuid() {
        return uuid;
    }

    public CameraLiveActionHelper(String uuid) {
        this.uuid = uuid;
    }

    public boolean isLive() {
        return isLive;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void onVideoDisconnected(JFGMsgVideoDisconn videoDisconn) {
        String remote = videoDisconn.remote;
        if (TextUtils.equals(remote, uuid)) {
            playCode = videoDisconn.code;
        }
    }

    public void setMicrophoneOn(boolean isMicrophoneOn) {
        this.isMicrophoneOn = isMicrophoneOn;
    }

    public boolean isMicrophoneOn() {
        return isMicrophoneOn;
    }

    public void setSpeakerOn(boolean speakerOn) {
        isSpeakerOn = speakerOn;
    }

    public boolean isSpeakerOn() {
        return isSpeakerOn;
    }

    public int checkPlayCode(boolean reset) {
        int code = playCode;
        if (reset) {
            playCode = 0;
        }
        return code;
    }

    public void onUpdateVideoPlayCode(int playCode) {
        this.playCode = playCode;
    }

    public void onVideoPlayStarted(boolean live) {
        this.isPlaying = true;
        onUpdateVideoPlayType(live);
    }

    public void onUpdateVideoPlayType(boolean live) {
        this.isLive = live;
    }

    public void onUpdateVideoSlowState(boolean slow) {
        isLiveSlow = slow;
    }

    public void onUpdateVideoFrameFailed() {
        isLiveBad = true;
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

    public boolean isPendingPlayActionCompleted() {
        return false;
    }

    public void onUpdateSDCard(boolean hasSdcard) {
        this.isSDCardExist = hasSdcard;
    }

    public void onUpdateStandBy(boolean standBy) {
        this.isStandBy = standBy;
    }

    public long getLastPlayTime() {
        return lastPlayTime;
    }
}
