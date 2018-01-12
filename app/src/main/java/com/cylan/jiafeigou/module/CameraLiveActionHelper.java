package com.cylan.jiafeigou.module;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.utils.PreferencesUtils;

/**
 * Created by yanzhendong on 2018/1/3.
 */

@SuppressWarnings("WeakerAccess")
public class CameraLiveActionHelper {
    public String uuid;
    public volatile boolean isLive = true;
    public volatile boolean isPlaying = false;
    public volatile boolean isLoading = false;
    public volatile boolean isSpeakerOn = false;
    public volatile boolean isMicrophoneOn = false;
    public volatile boolean isTalkBackMode = false;
    public volatile boolean isLiveSlow = false;
    public volatile boolean isLiveBad = false;
    public volatile boolean isStandBy = false;
    public volatile boolean isSDCardExist = false;
    public volatile boolean isSDCardFormatted = false;
    public volatile boolean isLocalOnline = false;
    public volatile boolean isDeviceOnline = true;
    public volatile int resolutionH;
    public volatile int resolutionW;
    public volatile DpMsgDefine.DPNet deviceNet;
    public volatile DpMsgDefine.DPTimeZone deviceTimezone;
    public volatile DpMsgDefine.DpCoordinate deviceCoordinate;
    public volatile String deviceViewMountMode;
    public volatile int deviceDisplayMode;
    public volatile boolean isDeviceAlarmOpened;
    public volatile int deviceStreamMode;
    public volatile int deviceBattery;
    public volatile int playCode;
    public volatile long lastPlayTime = 0;
    public volatile int lastReportedPlayError = 0;
    public volatile int lastUnKnowPlayError;
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
    public final boolean hasSDCardFeature;
    public final boolean hasDoorLockFeature;
    public final boolean hasSafeProtectionFeature;
    public final boolean hasSightFeature;
    public final boolean hasViewModeFeature;
    public final boolean hasMicrophoneFeature;

    public CameraLiveActionHelper(String uuid) {
        this.uuid = uuid;
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        this.hasSDCardFeature = JFGRules.hasSDFeature(device.pid);
        this.hasDoorLockFeature = JFGRules.hasDoorLock(device.pid);
        this.hasSafeProtectionFeature = JFGRules.hasProtection(device.pid, false);
        this.hasSightFeature = JFGRules.showSight(device.pid, false);
        this.hasViewModeFeature = JFGRules.showSwitchModeButton(device.pid);
        this.hasMicrophoneFeature = JFGRules.hasMicFeature(device.pid);
        this.deviceDisplayMode = PreferencesUtils.getInt(getSavedDisplayModeKey(), this.deviceDisplayMode);
    }

    public void onUpdateDeviceInformation() {
        Log.d(CameraLiveHelper.TAG, "onUpdateDeviceInformation");
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        this.isStandBy = JFGRules.isDeviceStandBy(device);
        this.isSDCardExist = JFGRules.isSDCardExist(device);
        this.deviceNet = device.$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
        this.deviceTimezone = device.$(DpMsgMap.ID_214_DEVICE_TIME_ZONE, new DpMsgDefine.DPTimeZone());
        this.isDeviceAlarmOpened = device.$(DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
        this.lastReportedPlayError = CameraLiveHelper.PLAY_ERROR_NO_ERROR;

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
            this.resolutionH = jfgMsgVideoResolution.height;
            this.resolutionW = jfgMsgVideoResolution.width;
            this.isPendingPlayLiveActionCompleted = true;
            this.isPendingStopLiveActionCompleted = true;
            this.isPendingPlayLiveActionTimeOutActionReached = false;
            this.isVideoResolutionReached = true;
            this.isLiveBad = false;
            this.isLiveSlow = false;
            this.isLoading = false;
            this.isPlaying = true;
            PreferencesUtils.putFloat(getSavedResolutionKey(), (float) jfgMsgVideoResolution.height / jfgMsgVideoResolution.width);
        }
    }

    public void onVideoPlayPrepared(boolean live) {
        this.isPlaying = false;
        this.isLoading = false;
        this.isLiveBad = false;
        this.isLiveSlow = false;
        this.isPendingStopLiveActionCompleted = true;
        this.isPendingPlayLiveActionCompleted = true;
        this.isVideoResolutionReached = false;
        this.isPendingPlayLiveActionTimeOutActionReached = false;
        this.isLive = live;
        this.lastReportedPlayError = CameraLiveHelper.PLAY_ERROR_NO_ERROR;
        this.lastUnKnowPlayError = CameraLiveHelper.PLAY_ERROR_NO_ERROR;
    }

    public void onVideoStopPrepared(boolean live) {
        this.isPendingStopLiveActionCompleted = false;
        this.isPendingPlayLiveActionCompleted = true;
        this.isLive = live;
    }

    public void onVideoPlayStopped(boolean live, int playCode) {
        this.isPlaying = false;
        this.isLoading = false;
        this.isLiveSlow = false;
        this.isPendingStopLiveActionCompleted = true;
        this.isPendingPlayLiveActionCompleted = true;
        this.isVideoResolutionReached = false;
        this.isLive = live;
        this.lastReportedPlayError = CameraLiveHelper.PLAY_ERROR_NO_ERROR;
        this.playCode = playCode;
    }

    public void onVideoPlayTimeOutReached() {
        this.isPendingPlayLiveActionTimeOutActionReached = true;
        this.isPendingStopLiveActionCompleted = true;
        this.isPendingPlayLiveActionCompleted = true;
        this.isLiveSlow = false;
    }

    public boolean isLoadingFailed() {
        return this.isLiveBad && this.isLiveSlow;
    }

    public void onUpdateHistoryVideoError(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {
        this.playCode = jfgHistoryVideoErrorInfo.code;
    }

    public boolean onUpdatePendingPlayLiveActionCompleted() {
        boolean isPendingPlayLiveActionCompleted = this.isPendingPlayLiveActionCompleted;
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
            this.isLiveSlow = false;
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

    public boolean onUpdateDeviceLocalOnlineState(boolean isLocalOnline) {
        final boolean localOnline = this.isLocalOnline;
        this.isLocalOnline = isLocalOnline;
        return localOnline;
    }

    public DpMsgDefine.DPNet onUpdateDeviceNet(DpMsgDefine.DPNet dpNet) {
        final DpMsgDefine.DPNet net = this.deviceNet;
        this.deviceNet = dpNet;
        return net;
    }

    public DpMsgDefine.DPTimeZone onUpdateDeviceTimezone(DpMsgDefine.DPTimeZone dpTimeZone) {
        final DpMsgDefine.DPTimeZone timeZone = this.deviceTimezone;
        this.deviceTimezone = dpTimeZone;
        return timeZone;
    }

    public DpMsgDefine.DpCoordinate onUpdateDeviceCoordinate(DpMsgDefine.DpCoordinate dpCoordinate) {
        final DpMsgDefine.DpCoordinate coordinate = this.deviceCoordinate;
        this.deviceCoordinate = dpCoordinate;
        return coordinate;
    }

    public String onUpdateDeviceMountMode(String mountMode) {
        final String viewMountMode = this.deviceViewMountMode;
        this.deviceViewMountMode = mountMode;
        return viewMountMode;
    }

    public int onUpdateDeviceBattery(Integer battery) {
        final int preBattery = this.deviceBattery;
        this.deviceBattery = battery == null ? 0 : battery;
        return preBattery;
    }

    public boolean onUpdateDeviceAlarmOpenState(Boolean alarmOpen) {
        final boolean isDeviceAlarmOpen = this.isDeviceAlarmOpened;
        this.isDeviceAlarmOpened = alarmOpen;
        return isDeviceAlarmOpen;
    }

    public String getSavedResolutionKey() {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        return JConstant.KEY_UUID_RESOLUTION + ":" + device.pid;
    }

    public String getSavedDisplayModeKey() {
        return "DEVICE_SAVED_DISPLAY_MODE_KEY_" + uuid;
    }

    public int onUpdateDeviceDisplayMode(int displayMode) {
        int deviceDisplayMode = this.deviceDisplayMode;
        this.deviceDisplayMode = displayMode;
        PreferencesUtils.putInt(getSavedDisplayModeKey(), displayMode);
        return deviceDisplayMode;
    }

    public boolean onUpdateMicrophoneOn(boolean isMicrophoneOn) {
        boolean on = this.isMicrophoneOn;
        this.isMicrophoneOn = isMicrophoneOn;
        this.isTalkBackMode = isMicrophoneOn;
        return on;
    }

    public boolean onUpdateSpeakerOn(boolean isSpeakerOn) {
        boolean on = this.isSpeakerOn;
        this.isSpeakerOn = isSpeakerOn;
        this.isTalkBackMode = isSpeakerOn && this.isTalkBackMode;
        return on;
    }

    public boolean onUpdatePendingCaptureActionCompleted() {
        boolean isPendingCaptureActionCompleted = this.isPendingCaptureActionCompleted;
        this.isPendingCaptureActionCompleted = true;
        return isPendingCaptureActionCompleted;
    }

    public int onUpdateDeviceStreamMode(int mode) {
        int streamMode = this.deviceStreamMode;
        this.deviceStreamMode = mode;
        return streamMode;
    }
}
