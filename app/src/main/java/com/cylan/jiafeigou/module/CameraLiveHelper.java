package com.cylan.jiafeigou.module;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import static com.cylan.jiafeigou.misc.JConstant.KEY_CAM_SIGHT_SETTING;

/**
 * Created by yanzhendong on 2018/1/3.
 */

public class CameraLiveHelper {
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
        return PreferencesUtils.getBoolean(KEY_CAM_SIGHT_SETTING + uuid, false);
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

        boolean badFrameState = helper.checkLiveBadFrameState(true);
        if (badFrameState) {
            return PLAY_ERROR_BAD_FRAME_RATE;
        }

        boolean lowFrameState = helper.checkLiveLowFrameState(true);
        if (lowFrameState) {
            return PLAY_ERROR_LOW_FRAME_RATE;
        }

        helper.isPendingPlayActionCompleted();

        return 0;
    }

    public static boolean shouldDisconnectFirst(CameraLiveActionHelper helper) {
        boolean playing = helper.isPlaying();
        int playCode = helper.checkPlayCode(false);
        boolean live = helper.isLive();
        return live && playing && playCode == 0;
    }

    public static boolean isVideoPlaying(CameraLiveActionHelper helper) {
        return helper.isPlaying();
    }

    public static boolean isLive(CameraLiveActionHelper helper) {
        return helper.isLive();
    }

    public static boolean checkMicrophoneEnable(CameraLiveActionHelper helper) {
        boolean playing = helper.isPlaying();
        int playCode = helper.checkPlayCode(false);
        boolean badFrameState = helper.checkLiveBadFrameState(false);
        boolean lowFrameState = helper.checkLiveLowFrameState(false);
        return playing && playCode == 0 && !badFrameState && !lowFrameState;
    }

    public static boolean checkSpeakerEnable(CameraLiveActionHelper helper) {
        boolean live = helper.isLive();
        boolean playing = helper.isPlaying();
        int playCode = helper.checkPlayCode(false);
        boolean badFrameState = helper.checkLiveBadFrameState(false);
        boolean lowFrameState = helper.checkLiveLowFrameState(false);
        return live && playing && playCode == 0 && !badFrameState && !lowFrameState;
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
}
