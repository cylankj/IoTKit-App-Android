package com.cylan.jiafeigou.module;

import android.Manifest;
import android.media.MediaRecorder;
import android.os.Build;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import permissions.dispatcher.PermissionUtils;

import static com.cylan.jiafeigou.misc.JConstant.KEY_CAM_SIGHT_SETTING;

/**
 * Created by yanzhendong on 2018/1/3.
 */

public class CameraLiveHelper {
    public static final String TAG = CameraLiveHelper.class.getSimpleName();
    public static final int PLAY_ERROR_NO_ERROR = 0;
    public static final int PLAY_ERROR_STANDBY = 1;
    public static final int PLAY_ERROR_FIRST_SIGHT = 2;
    public static final int PLAY_ERROR_NO_NETWORK = 3;
    public static final int PLAY_ERROR_DEVICE_OFF_LINE = 4;
    public static final int PLAY_ERROR_JFG_EXCEPTION_THROW = 5;
    public static final int PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED = 6;
    public static final int PLAY_ERROR_LOW_FRAME_RATE = 7;
    public static final int PLAY_ERROR_BAD_FRAME_RATE = 8;
    public static final int PLAY_ERROR_IN_CONNECTING = 9;
    public static final int PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED_TIME_OUT = 10;
    public static final int PLAY_ERROR_UN_KNOW_PLAY_ERROR = 11;
    public static int unKnowErrorCode = 0;

    public static String printError(int playError) {
        switch (playError) {
            case PLAY_ERROR_NO_ERROR:
                return "PLAY_ERROR_NO_ERROR";
            case PLAY_ERROR_STANDBY:
                return "PLAY_ERROR_STANDBY";
            case PLAY_ERROR_FIRST_SIGHT:
                return "PLAY_ERROR_FIRST_SIGHT";
            case PLAY_ERROR_NO_NETWORK:
                return "PLAY_ERROR_NO_NETWORK";
            case PLAY_ERROR_DEVICE_OFF_LINE:
                return "PLAY_ERROR_DEVICE_OFF_LINE";
            case PLAY_ERROR_JFG_EXCEPTION_THROW:
                return "PLAY_ERROR_JFG_EXCEPTION_THROW";
            case PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED:
                return "PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED";
            case PLAY_ERROR_LOW_FRAME_RATE:
                return "PLAY_ERROR_LOW_FRAME_RATE";
            case PLAY_ERROR_BAD_FRAME_RATE:
                return "PLAY_ERROR_BAD_FRAME_RATE";
            case PLAY_ERROR_IN_CONNECTING:
                return "PLAY_ERROR_IN_CONNECTING";
            default:
                return "Unknown PlayError:" + playError;
        }
    }

    public static boolean canPlayVideoNow(String uuid) {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        //待机模式
        boolean standBy = device.$(508, new DpMsgDefine.DPStandby()).standby;
        //全景,首次使用模式
        boolean sightShow = PreferencesUtils.getBoolean(KEY_CAM_SIGHT_SETTING + uuid, false);
        return !standBy && !sightShow;
    }

    public static boolean canShowFlip(String uuid) {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        return !JFGRules.isShareDevice(device) && JFGRules.hasProtection(device.pid, false);
    }

    public static boolean canShowHistoryWheel(String uuid) {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        return !JFGRules.isShareDevice(device) && JFGRules.hasSDFeature(device.pid);
    }

    public static boolean isDeviceStandby(String uuid) {
        //待机模式
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        return device.$(508, new DpMsgDefine.DPStandby()).standby;
    }

    public static boolean isFirstSight(String uuid) {
        //全景,首次使用模式
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        boolean shareDevice = JFGRules.isShareDevice(uuid);
        boolean showSight = JFGRules.showSight(device.pid, shareDevice);
        boolean hasShowSight = PreferencesUtils.getBoolean(KEY_CAM_SIGHT_SETTING + uuid, false);
        return !shareDevice && showSight && hasShowSight;
    }

    public static boolean isDeviceOnline(String uuid) {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        return JFGRules.isDeviceOnline(net);
    }

    /**
     * @return 环境检查 0:当前环境可以播放;1:设备开启了待机;2:首次使用全景模式;3:无网络连接;4:设备离线;5:已经加载中
     */
    public static int checkPlayError(CameraLiveActionHelper helper) {
        String uuid = helper.getUuid();
        boolean deviceStandby = isDeviceStandby(uuid);
        if (deviceStandby) {
            return PLAY_ERROR_STANDBY;
        }

        boolean firstSight = isFirstSight(uuid);
        if (firstSight) {
            return PLAY_ERROR_FIRST_SIGHT;
        }

        int netType = NetUtils.getJfgNetType();
        if (netType == 0) {
            return PLAY_ERROR_NO_NETWORK;
        }

        boolean deviceOnline = isDeviceOnline(uuid);
        if (!deviceOnline) {
            return PLAY_ERROR_DEVICE_OFF_LINE;
        }

        int playCode = helper.checkPlayCode(true);
        if (playCode == PLAY_ERROR_JFG_EXCEPTION_THROW) {
            return PLAY_ERROR_JFG_EXCEPTION_THROW;
        }

        if (playCode == JError.ErrorVideoPeerInConnect) {
            return PLAY_ERROR_IN_CONNECTING;
        }
        if (playCode != 0) {
            unKnowErrorCode = playCode;
            return PLAY_ERROR_UN_KNOW_PLAY_ERROR;
        }
        boolean playActionCompleted = helper.isPendingPlayActionCompleted();
        if (!playActionCompleted) {
            return PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED;
        }
        boolean badFrameState = helper.checkLiveBadFrameState(true);
        if (badFrameState) {
            return PLAY_ERROR_BAD_FRAME_RATE;
        }

        boolean lowFrameState = helper.checkLiveLowFrameState(true);
        if (lowFrameState) {
            return PLAY_ERROR_LOW_FRAME_RATE;
        }

        boolean loadingFailed = helper.isLoadingFailed();
        if (loadingFailed) {
            return PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED_TIME_OUT;
        }

        return PLAY_ERROR_NO_ERROR;
    }

    public static boolean shouldDisconnectFirst(CameraLiveActionHelper helper) {
        boolean playing = helper.isPlaying();
        int playCode = helper.checkPlayCode(false);
        boolean live = helper.isLive();
        return live && playing && playCode == 0;
    }

    public static boolean isVideoPlaying(CameraLiveActionHelper helper) {
        return helper.isPlaying() && helper.isPendingPlayActionCompleted() && NetUtils.getJfgNetType() != 0;
    }

    public static boolean isLive(CameraLiveActionHelper helper) {
        return helper.isLive();
    }

    public static boolean checkMicrophoneEnable(CameraLiveActionHelper helper) {
        boolean live = helper.isLive();
        boolean playing = helper.isPlaying();
        int playCode = helper.checkPlayCode(false);
        boolean badFrameState = helper.checkLiveBadFrameState(false);
        boolean lowFrameState = helper.checkLiveLowFrameState(false);
        boolean playActionCompleted = helper.isPendingPlayActionCompleted();
        return live && playing && playCode == 0 && !badFrameState && !lowFrameState && playActionCompleted;
    }

    public static boolean checkSpeakerEnable(CameraLiveActionHelper helper) {
        boolean playing = helper.isPlaying();
        int playCode = helper.checkPlayCode(false);
        boolean badFrameState = helper.checkLiveBadFrameState(false);
        boolean lowFrameState = helper.checkLiveLowFrameState(false);
        boolean playActionCompleted = helper.isPendingPlayActionCompleted();
        return playing && playCode == 0 && !badFrameState && !lowFrameState && playActionCompleted;
    }

    public static boolean checkDoorLockEnable(CameraLiveActionHelper helper) {
        return false;
    }

    public static boolean checkCaptureEnable(CameraLiveActionHelper helper) {
        boolean playing = helper.isPlaying();
        int playCode = helper.checkPlayCode(false);
        boolean badFrameState = helper.checkLiveBadFrameState(false);
        boolean lowFrameState = helper.checkLiveLowFrameState(false);
        return playing && playCode == 0 && !badFrameState && !lowFrameState;
    }

    public static long getLastPlayTime(boolean live, CameraLiveActionHelper liveActionHelper) {
        return live ? 0 : liveActionHelper.getLastPlayTime();
    }

    public static boolean isVideoLoading(CameraLiveActionHelper helper) {
        return helper.isLoading();
    }

    public static boolean checkMicrophoneOn(CameraLiveActionHelper liveActionHelper, boolean speakerOn) {
        return liveActionHelper.isMicrophoneOn();
    }

    public static boolean checkSpeakerOn(CameraLiveActionHelper liveActionHelper, boolean microphoneOn) {
        return microphoneOn || liveActionHelper.isSpeakerOn();
    }

    public static boolean checkAudioPermission() {
        MediaRecorder mRecorder = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//这是为了兼容魅族4.4的权限
            try {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.release();
            } catch (Exception e) {
                AppLogger.d(e.getMessage());
                if (mRecorder != null) {
                    mRecorder.release();
                }
                return false;
            }
        } else {
            if (!PermissionUtils.hasSelfPermissions(ContextUtils.getContext(), Manifest.permission.RECORD_AUDIO)) {
                return false;
            }
        }
        return true;
    }

    public static int checkUnKnowErrorCode(boolean reset) {
        int code = unKnowErrorCode;
        if (reset) {
            unKnowErrorCode = 0;
        }
        return code;
    }

    public static boolean checkSyncEvent(CameraLiveActionHelper helper, int event) {
        return (helper.getSyncEvent() & event) == 1;
    }
}
