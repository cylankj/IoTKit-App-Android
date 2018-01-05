package com.cylan.jiafeigou.module;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;

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
    private volatile boolean isPendingPlayActionCompleted = true;
    private volatile int syncEvent = 0;
    public static final int SYNC_EVENT_WAIT_FOR_STOP_COMPLETED = 1;
    public static final int SYNC_EVENT_WAIT_FOR_CAPTURE_COMPLETED = 1 << 1;

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
            isPendingPlayActionCompleted = true;
        }
    }

    public boolean isMicrophoneOn() {
        return isMicrophoneOn;
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
        this.isPendingPlayActionCompleted = false;
        this.isLoading = true;
        this.isLiveBad = false;
        this.isLiveSlow = false;
        onUpdateVideoPlayType(live);
    }

    public void onUpdateVideoPlayType(boolean live) {
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

    public boolean isPendingPlayActionCompleted() {
        return isPendingPlayActionCompleted;
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

    public void onVideoResolutionReached(JFGMsgVideoResolution jfgMsgVideoResolution) {
        if (TextUtils.equals(uuid, jfgMsgVideoResolution.peer)) {
            isPendingPlayActionCompleted = true;
            isLiveBad = false;
            isLiveSlow = false;
            isLoading = false;
            isPlaying = true;
        }
    }

    public void onVideoPlayStopped(boolean live) {
        this.isPlaying = false;
        this.isPendingPlayActionCompleted = true;
        this.isLoading = false;
        this.isLiveBad = false;
        this.isLiveSlow = false;
        syncEvent = syncEvent & (Integer.MAX_VALUE ^ SYNC_EVENT_WAIT_FOR_STOP_COMPLETED);
        onUpdateVideoPlayType(live);
    }

    public void onUpdateVideoPlayTimeOutAction() {
        isLiveBad = true;
        isLiveSlow = true;
    }

    public boolean isLoadingFailed() {
        return isLiveBad && isLiveSlow;
    }

    public void onUpdateSpeakerOn(boolean speakerOn) {
        this.isSpeakerOn = speakerOn;
    }

    public void onUpdateMicrophoneOn(boolean microphoneOn) {
        this.isMicrophoneOn = microphoneOn;
    }

    public void onUpdateLastPlayTime(long lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    public void onUpdatePendingPlayAction(boolean isPendingPlayActionCompleted) {
        this.isPendingPlayActionCompleted = isPendingPlayActionCompleted;
    }

    public void onUpdateHistoryVideoError(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {
        this.playCode = jfgHistoryVideoErrorInfo.code;
    }

    public void onUpdateSyncAction(int syncEvent) {
        this.syncEvent |= syncEvent;
    }

    public int getSyncEvent() {
        return syncEvent;
    }
}
