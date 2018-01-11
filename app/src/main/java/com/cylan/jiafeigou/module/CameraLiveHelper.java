package com.cylan.jiafeigou.module;

import android.Manifest;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.panorama.Panoramic360ViewRS;

import java.io.File;
import java.nio.ByteBuffer;

import permissions.dispatcher.PermissionUtils;

import static com.cylan.jiafeigou.misc.JConstant.KEY_CAM_SIGHT_SETTING;

/**
 * Created by yanzhendong on 2018/1/3.
 */

public class CameraLiveHelper {
    public static final String TAG = CameraLiveHelper.class.getSimpleName();
    public static final String VERB_TAG = "VERB:" + CameraLiveHelper.class.getSimpleName();
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
    public static final int PLAY_ERROR_SD_FILE_IO = 12;
    public static final int PLAY_ERROR_SD_IO = 13;
    public static final int PLAY_ERROR_SD_HISTORY_ALL = 14;
    public static final int PLAY_ERROR_VIDEO_PEER_NOT_EXIST = 15;
    public static final int PLAY_ERROR_VIDEO_PEER_DISCONNECT = 16;

    private static final int MAX_CACHE_SIZE = (int) (Runtime.getRuntime().totalMemory() / 1024 / 8);
    public static LruCache<String, byte[]> sLiveThumbLruCache = new LruCache<String, byte[]>(MAX_CACHE_SIZE) {
        @Override
        protected int sizeOf(String key, byte[] value) {
            return value.length;
        }
    };

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
            case PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED_TIME_OUT:
                return "PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED_TIME_OUT";
            case PLAY_ERROR_UN_KNOW_PLAY_ERROR:
                return "PLAY_ERROR_UN_KNOW_PLAY_ERROR";
            case PLAY_ERROR_SD_FILE_IO:
                return "PLAY_ERROR_SD_FILE_IO";
            case PLAY_ERROR_SD_IO:
                return "PLAY_ERROR_SD_IO";
            case PLAY_ERROR_SD_HISTORY_ALL:
                return "PLAY_ERROR_SD_HISTORY_ALL";
            case PLAY_ERROR_VIDEO_PEER_NOT_EXIST:
                return "PLAY_ERROR_VIDEO_PEER_NOT_EXIST";
            case PLAY_ERROR_VIDEO_PEER_DISCONNECT:
                return "PLAY_ERROR_VIDEO_PEER_DISCONNECT";
            default:
                return "Unknown PlayError:" + playError;
        }
    }

    public static boolean canPlayVideoNow(CameraLiveActionHelper helper) {
        Device device = DataSourceManager.getInstance().getDevice(helper.uuid);
        //待机模式
        boolean standBy = device.$(508, new DpMsgDefine.DPStandby()).standby;
        //全景,首次使用模式
        boolean sightShow = PreferencesUtils.getBoolean(KEY_CAM_SIGHT_SETTING + helper.uuid, false);
        return !standBy && !sightShow;
    }

    public static boolean canShowFlip(CameraLiveActionHelper helper) {
        Device device = DataSourceManager.getInstance().getDevice(helper.uuid);
        return !JFGRules.isShareDevice(device)
                && helper.hasSafeProtectionFeature;
    }

    public static boolean canShowHistoryWheel(CameraLiveActionHelper helper) {
        Device device = DataSourceManager.getInstance().getDevice(helper.uuid);
        return !JFGRules.isShareDevice(device)
                && helper.hasSDCardFeature
                && JFGRules.isDeviceOnline(helper.uuid)
                && NetUtils.hasNetwork()
                && !isDeviceStandby(helper)
                && !isFirstSight(helper);
    }

    public static boolean canShowStreamSwitcher(CameraLiveActionHelper helper) {
        Device device = DataSourceManager.getInstance().getDevice(helper.uuid);
        return isVideoPlaying(helper)
                && isLive(helper)
                && JFGRules.showSdHd(device.pid, device.$(207, ""), false);
    }

    public static boolean isDeviceStandby(CameraLiveActionHelper helper) {
        //待机模式
        Device device = DataSourceManager.getInstance().getDevice(helper.uuid);
        return device.$(508, new DpMsgDefine.DPStandby()).standby;
    }

    public static boolean isFirstSight(CameraLiveActionHelper helper) {
        //全景,首次使用模式
        boolean shareDevice = JFGRules.isShareDevice(helper.uuid);
        boolean showSight = helper.hasSightFeature;
        boolean hasShowSight = PreferencesUtils.getBoolean(KEY_CAM_SIGHT_SETTING + helper.uuid, false);
        //for test
        return !shareDevice && showSight && hasShowSight;
    }

    public static void resetFirstSight(CameraLiveActionHelper helper) {
        PreferencesUtils.putBoolean(KEY_CAM_SIGHT_SETTING + helper.uuid, false);
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
        String uuid = helper.uuid;
        int playError = PLAY_ERROR_NO_ERROR;
        int playCode = helper.checkPlayCode(true);
        if (isDeviceStandby(helper)) {
            playError = PLAY_ERROR_STANDBY;
        } else if (isFirstSight(helper)) {
            playError = PLAY_ERROR_FIRST_SIGHT;
        } else if (!NetUtils.hasNetwork()) {
            playError = PLAY_ERROR_NO_NETWORK;
        } else if (!isDeviceOnline(uuid)) {
            playError = PLAY_ERROR_DEVICE_OFF_LINE;
        } else if (helper.checkPlayTimeout(false)) {
            playError = PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED_TIME_OUT;
        } else if (!helper.isPendingPlayLiveActionCompleted) {
            playError = PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED;
        } else if (helper.checkLiveBadFrameState(false)) {
            playError = PLAY_ERROR_BAD_FRAME_RATE;
        } else if (helper.checkLiveLowFrameState(false)) {
            playError = PLAY_ERROR_LOW_FRAME_RATE;
        } else if (helper.lastUnKnowPlayError != PLAY_ERROR_NO_ERROR) {
            playError = PLAY_ERROR_UN_KNOW_PLAY_ERROR;
        } else if (playCode == PLAY_ERROR_JFG_EXCEPTION_THROW) {
            playError = PLAY_ERROR_JFG_EXCEPTION_THROW;
        } else if (playCode == JError.ErrorVideoPeerInConnect) {
            playError = PLAY_ERROR_IN_CONNECTING;
        } else if (playCode == JError.ErrorSDIO) {
            playError = PLAY_ERROR_SD_IO;
        } else if (playCode == JError.ErrorSDFileIO) {
            playError = PLAY_ERROR_SD_FILE_IO;
        } else if (playCode == JError.ErrorSDHistoryAll) {
            playError = PLAY_ERROR_SD_HISTORY_ALL;
        } else if (playCode == JError.ErrorVideoPeerNotExist) {
            playError = PLAY_ERROR_VIDEO_PEER_NOT_EXIST;
        } else if (playCode == JError.ErrorVideoPeerDisconnect) {
            playError = PLAY_ERROR_VIDEO_PEER_DISCONNECT;
        } else if (playCode != 0) {
            helper.lastUnKnowPlayError = playCode;
            playError = PLAY_ERROR_UN_KNOW_PLAY_ERROR;
        }
        Log.d(VERB_TAG, "check play error:" + printError(playError));
        return playError;
    }

    public static boolean shouldDisconnectFirst(CameraLiveActionHelper helper) {
        return shouldDisconnectFirst(helper, helper.playCode);
    }

    public static boolean shouldDisconnectFirst(CameraLiveActionHelper helper, int playCode) {
        boolean playing = helper.isPlaying;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "shouldDisconnectFirst? playCode is:" + playCode + ",如果 playCode 为1002 也应该断开,其他情况以后慢慢添加");
        }
        boolean live = helper.isLive;
        return live && ((playing && playCode == 0) || playCode == 1002);
    }

    public static boolean isVideoPlaying(CameraLiveActionHelper helper) {
        return helper.isPlaying && helper.isPendingPlayLiveActionCompleted && NetUtils.getJfgNetType() != 0;
    }

    public static boolean isLive(CameraLiveActionHelper helper) {
        return helper.isLive;
    }

    public static boolean checkMicrophoneEnable(CameraLiveActionHelper helper) {
        boolean live = isLive(helper);
        boolean videoLoading = isVideoLoading(helper);
        boolean videoPlaying = isVideoPlaying(helper);
        boolean hasNoPlayError = isNoError(helper);
        return live && !videoLoading && videoPlaying && hasNoPlayError;
    }

    public static boolean checkSpeakerEnable(CameraLiveActionHelper helper) {
        boolean videoLoading = isVideoLoading(helper);
        boolean videoPlaying = isVideoPlaying(helper);
        boolean hasNoPlayError = isNoError(helper);
        return !videoLoading && videoPlaying && hasNoPlayError;
    }

    public static boolean checkDoorLockEnable(CameraLiveActionHelper helper) {
        //无网络连接或者设备离线不可点击,局域网在线可点击,
        Device device = DataSourceManager.getInstance().getDevice(helper.uuid);
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        boolean deviceOnline = JFGRules.isDeviceOnline(net);
        boolean hasNetwork = NetUtils.hasNetwork();
        boolean isLocalOnline = helper.isLocalOnline;
        boolean isShareAccount = !TextUtils.isEmpty(device.shareAccount);
        return hasNetwork && !isShareAccount && (deviceOnline || isLocalOnline);
    }

    public static boolean checkCaptureEnable(CameraLiveActionHelper helper) {
        boolean videoLoading = isVideoLoading(helper);
        boolean videoPlaying = isVideoPlaying(helper);
        boolean hasNoPlayError = isNoError(helper);
        return !videoLoading && videoPlaying && hasNoPlayError;
    }

    public static long getLastPlayTime(boolean live, CameraLiveActionHelper liveActionHelper) {
        return live ? 0 : liveActionHelper.lastPlayTime;
    }

    public static boolean isVideoLoading(CameraLiveActionHelper helper) {
        return helper.isLoading;
    }

    public static boolean checkMicrophoneOn(CameraLiveActionHelper liveActionHelper, boolean speakerOn) {
        return liveActionHelper.isMicrophoneOn;
    }

    public static boolean checkSpeakerOn(CameraLiveActionHelper liveActionHelper, boolean microphoneOn) {
        return microphoneOn || liveActionHelper.isSpeakerOn;
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

    public static int checkUnKnowErrorCode(CameraLiveActionHelper helper, boolean reset) {
        int code = helper.lastUnKnowPlayError;
        if (reset) {
            helper.lastUnKnowPlayError = 0;
        }
        return code;
    }

    public static boolean checkFrameSlow(CameraLiveActionHelper helper, boolean slow) {
        boolean preSlow = helper.onUpdateVideoSlowState(slow);
        return !preSlow && slow;
    }

    public static boolean checkFrameBad(CameraLiveActionHelper helper) {
        return helper.isLiveBad && helper.isPendingPlayLiveActionCompleted && (helper.isVideoResolutionReached || System.currentTimeMillis() - helper.lastPlayTime > 30_000);
    }

    public static Bitmap checkLastLiveThumbPicture(CameraLiveActionHelper helper) {
        Bitmap lastLiveThumbPicture = helper.lastLiveThumbPicture;
        if (lastLiveThumbPicture != null && !lastLiveThumbPicture.isRecycled()) {
            helper.isLastLiveThumbPictureChanged = false;
        } else {
            lastLiveThumbPicture = getCache(helper);
            Log.d(TAG, "hasCache:" + (lastLiveThumbPicture != null));
            if (lastLiveThumbPicture == null) {
                String filePath = JConstant.MEDIA_PATH + File.separator + helper.uuid + "_cover.png";
                lastLiveThumbPicture = BitmapFactory.decodeFile(filePath);
                if (lastLiveThumbPicture != null) {
                    putCache(helper, lastLiveThumbPicture);
                }
            }
            helper.isLastLiveThumbPictureChanged = true;
        }
        helper.lastLiveThumbPicture = lastLiveThumbPicture;
        return lastLiveThumbPicture;
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteBuffer buf = ByteBuffer.allocate(bitmap.getWidth() * bitmap.getHeight() * 4);
        bitmap.copyPixelsToBuffer(buf);
        return buf.array();
    }

    public static void putCache(CameraLiveActionHelper helper, Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            byte[] bytes = bitmapToByteArray(bitmap);
            sLiveThumbLruCache.put(makeCacheKey(helper), bytes);
        }
    }

    public static Bitmap getCache(CameraLiveActionHelper helper) {
        byte[] bytes = sLiveThumbLruCache.get(makeCacheKey(helper));
        if (bytes != null) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        return null;
    }

    public static String makeCacheKey(CameraLiveActionHelper helper) {
        return helper.uuid + ":cache";
    }

    public static boolean checkIsPanoramaView(CameraLiveActionHelper helper) {
        Device device = DataSourceManager.getInstance().getDevice(helper.uuid);
        return JFGRules.isRoundRadio(device.pid);
    }

    public static boolean canStopVideoWithPlayError(int playError) {
        return true;
    }


    public static void waitForStopCompleted(CameraLiveActionHelper helper) {
        int waitCount = 0;
        do {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "正在等待视频结束完成!!!");
            }
            SystemClock.sleep(700);
            waitCount++;
        } while (waitCount < 10 && !helper.isPendingStopLiveActionCompleted);
    }

    public static void waitForCaptureCompleted(CameraLiveActionHelper helper) {
        int waitCount = 0;
        do {
            if (BuildConfig.DEBUG) {
                Log.d(CameraLiveHelper.TAG, "正在等待截图结束.....");
            }
            SystemClock.sleep(500);
            waitCount++;
        } while (waitCount < 10 && !helper.isPendingCaptureActionCompleted);
    }

    public static boolean isNoError(CameraLiveActionHelper helper) {
        return checkPlayError(helper) == PLAY_ERROR_NO_ERROR;
    }

    public static boolean isVideoStopped(CameraLiveActionHelper helper) {
        return !helper.isPlaying && helper.isPendingStopLiveActionCompleted;
    }

    public static boolean shouldReportError(CameraLiveActionHelper helper, int playError) {
        int lastPlayError = helper.lastReportedPlayError;
        return lastPlayError != playError || playError == CameraLiveHelper.PLAY_ERROR_UN_KNOW_PLAY_ERROR;
    }

    public static long LongTimestamp(long timestamp) {
        return System.currentTimeMillis() > timestamp * 100 ? timestamp * 1000 : timestamp;
    }

    public static boolean checkIsThumbPictureChanged(CameraLiveActionHelper helper) {
        return true;
    }

    public static boolean checkIsDeviceNetChanged(CameraLiveActionHelper helper, DpMsgDefine.DPNet dpNet) {
        return dpNet == null || helper.deviceNet == null || dpNet.net != helper.deviceNet.net || !TextUtils.equals(dpNet.ssid, helper.deviceNet.ssid);
    }

    public static boolean checkIsDeviceTimeZoneChanged(CameraLiveActionHelper helper, DpMsgDefine.DPTimeZone timeZone) {
        return helper.deviceTimezone == null || timeZone == null || helper.deviceTimezone.offset != timeZone.offset;
    }

    public static boolean shouldResumeToPlayVideo(CameraLiveActionHelper helper) {
        return canPlayVideoNow(helper) && helper.hasPendingResumeToPlayVideoAction;
    }


    public static boolean checkIsDeviceLocalOnlineChanged(CameraLiveActionHelper helper, boolean localOnlineState) {
        return helper.isLocalOnline != localOnlineState;
    }

    public static boolean checkIsDeviceCoordinateChanged(CameraLiveActionHelper helper, DpMsgDefine.DpCoordinate dpCoordinate) {
        return dpCoordinate == null || helper.deviceCoordinate == null
                || dpCoordinate.h != helper.deviceCoordinate.h
                || dpCoordinate.r != helper.deviceCoordinate.r
                || dpCoordinate.w != helper.deviceCoordinate.w
                || dpCoordinate.x != helper.deviceCoordinate.x
                || dpCoordinate.y != helper.deviceCoordinate.y;
    }

    public static boolean checkIsDeviceViewMountModeChanged(CameraLiveActionHelper helper, String viewMountMode) {
        return TextUtils.isEmpty(viewMountMode) || TextUtils.isEmpty(helper.deviceViewMountMode) || !TextUtils.equals(viewMountMode, helper.deviceViewMountMode);
    }

    public static boolean checkIsDeviceBatteryChanged(CameraLiveActionHelper helper, Integer battery) {
        return battery == null || helper.deviceBattery != battery;
    }

    public static boolean checkIsDeviceAlarmOpenStateChanged(CameraLiveActionHelper helper, Boolean alarmOpen) {
        return alarmOpen == null || alarmOpen != helper.isDeviceAlarmOpened;
    }

    public static boolean canShowHistoryCase(CameraLiveActionHelper helper) {
        return PreferencesUtils.getBoolean(JConstant.KEY_SHOW_HISTORY_WHEEL_CASE, true);
    }

    public static float checkVideoRadio(CameraLiveActionHelper helper, boolean isLand) {
        Device device = DataSourceManager.getInstance().getDevice(helper.uuid);
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        final boolean needPanoramicView = JFGRules.isNeedPanoramicView(device.pid);
        float liveViewRadio = 0F;
        if (helper.resolutionH != 0 && helper.resolutionW != 0) {
            liveViewRadio = (float) helper.resolutionH / (float) helper.resolutionW;
        }
        if (liveViewRadio == 0) {
            liveViewRadio = PreferencesUtils.getFloat(helper.getSavedResolutionKey(), JFGRules.getDefaultPortHeightRatio(device.pid));
        }
        if (device.pid == 81) {//81设备特殊对待
            return isLand ? 3F / 4F : 1F;
        }

        if (isLand) {
            return (float) displayMetrics.heightPixels / (float) displayMetrics.widthPixels;
        } else {
            return needPanoramicView ? 1F : liveViewRadio;
        }
    }

    public static boolean canLoadHistoryEnable(CameraLiveActionHelper helper) {
        Device device = DataSourceManager.getInstance().getDevice(helper.uuid);
        boolean standBy = JFGRules.isDeviceStandBy(device);
        boolean deviceOnline = JFGRules.isDeviceOnline(helper.uuid);
        boolean historyPlayActionCompleted = helper.isPendingHistoryPlayActionCompleted;
        return !standBy && deviceOnline && historyPlayActionCompleted;
    }

    public static boolean canModeSwitchEnable(CameraLiveActionHelper helper) {
        boolean live = isLive(helper);
        boolean videoPlaying = isVideoPlaying(helper);
        boolean isLoading = helper.isLoading;
        boolean showSwitchModeButton = helper.hasViewModeFeature;
        return live && videoPlaying && showSwitchModeButton && !isLoading;
    }

    public static boolean canXunHuanEnable(CameraLiveActionHelper helper) {
        boolean modeSwitchEnable = canModeSwitchEnable(helper);
        boolean isCylinderDisplayMode = helper.deviceDisplayMode == Panoramic360ViewRS.SFM_Cylinder;
        return modeSwitchEnable && isCylinderDisplayMode;
    }

    /**
     * @return 0:无错误,
     * 1:当前设备支持视角切换但未设置成倒挂模式
     * 2:当前设备不支持视角切换但支持视图切换,强制开启平视视图
     * 3:当前设备不支持视图切换
     */
    public static int checkViewModeError(CameraLiveActionHelper helper) {
        Device device = DataSourceManager.getInstance().getDevice(helper.uuid);
        boolean hasViewAngle = JFGRules.hasViewAngle(device.pid);
        boolean hasViewMode = JFGRules.showSwitchModeButton(device.pid);
        String mountMode = device.$(DpMsgMap.ID_509_CAMERA_MOUNT_MODE, "1");
        int errorCode = 0;
        if (hasViewMode && hasViewAngle && TextUtils.equals("1", mountMode)) {
            errorCode = 1;
        } else if (hasViewMode && !hasViewAngle && TextUtils.equals("1", mountMode)) {
            errorCode = 2;
        } else if (!hasViewMode) {
            errorCode = 3;
        }
        return errorCode;
    }

    public static boolean canShowXunHuan(CameraLiveActionHelper helper) {
        return helper.hasViewModeFeature;
    }

    public static boolean canShowDoorLock(CameraLiveActionHelper helper) {
        Device device = DataSourceManager.getInstance().getDevice(helper.uuid);
        return helper.hasDoorLockFeature && TextUtils.isEmpty(device.shareAccount);
    }

    public static boolean canShowMicrophone(CameraLiveActionHelper helper) {
        return helper.hasMicrophoneFeature;
    }

    public static boolean canStreamSwitcherEnable(CameraLiveActionHelper helper) {
        return isVideoPlaying(helper);
    }

    public static boolean canShowViewModeMenu(CameraLiveActionHelper helper) {
        Device device = DataSourceManager.getInstance().getDevice(helper.uuid);
        return JFGRules.showSwitchModeButton(device.pid) && isVideoPlaying(helper) && isLive(helper);
    }

}
