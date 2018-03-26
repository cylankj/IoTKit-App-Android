package com.cylan.jiafeigou.n.mvp.contract.cam;

import android.graphics.Bitmap;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.mvp.BaseFragmentView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.panorama.CameraParam;

import java.util.Collection;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface CamLiveContract {

    /**
     * 默认直播
     */
    int TYPE_NONE = 0;
    int TYPE_LIVE = 1;
    int TYPE_HISTORY = 2;


    interface View extends BaseFragmentView {

        void onRtcp(JFGMsgVideoRtcp rtcp);

        void onResolution(JFGMsgVideoResolution resolution) throws JfgException;

        void showFirmwareDialog();

        void onBatteryDrainOut();

        void onDeviceUnBind();

        void onOpenDoorError();

        void onOpenDoorSuccess();

        void onOpenDoorPasswordError();

        void onHistoryReached(Collection<JFGVideo> history, boolean isHistoryEmpty);

        void onLoadHistoryFailed();

        void onDeviceStandByChanged(boolean isStandBy);

        void onPlayErrorFirstSight();

        void onPlayErrorNoNetwork();

        void onDeviceChangedToOffLine();

        void onDeviceChangedToOnline();

        void onPlayErrorException();

        void onPlayErrorWaitForPlayCompleted();

        void onPlayErrorLowFrameRate();

        void onPlayFrameResumeGood();

        void onPlayErrorBadFrameRate();

        void onUpdateBottomMenuEnable(boolean microphoneEnable, boolean speakerEnable, boolean doorLockEnable, boolean captureEnable);

        void onUpdateBottomMenuOn(boolean speakerOn, boolean microphoneOn);

        void onDeviceSDCardOut();

        void onDeviceSDCardFormat();

        void onUpdateLiveViewMode(String _509);

        void onDeviceTimeZoneChanged(int rawOffset);

        void onUpdateCameraCoordinate(DpMsgDefine.DpCoordinate dpCoordinate);

        void onVideoPlayStopped(boolean live);

        void onPlayErrorWaitForPlayCompletedTimeout();

        void onPlayErrorUnKnowPlayError(int errorCode);

        void onPlayErrorInConnecting();

        void onVideoPlayActionCompleted();

        void onCaptureFinished(Bitmap bitmap);

        void onUpdateNormalThumbPicture(Bitmap bitmap);

        void onUpdatePanoramaThumbPicture(Bitmap bitmap);

        void onNetworkResumeGood();

        void onDeviceNetChanged(DpMsgDefine.DPNet net, boolean isLocalOnline);

        void onUpdateAlarmOpenChanged(boolean alarmOpened);

        void onChangeSafeProtectionErrorAutoRecordClosed();

        void onChangeSafeProtectionErrorNeedConfirm();

        void onPlayErrorSDFileIO();

        void onPlayErrorSDHistoryAll();

        void onPlayErrorSDIO();

        void onPlayErrorVideoPeerDisconnect();

        void onPlayErrorVideoPeerNotExist();

        void onViewModeAvailable(int displayMode);

        void onViewModeHangError();

        void onViewModeNotSupportError();

        void onViewModeForceHangError(int displayMode);

        void onVideoPlayPrepared(boolean live);

        void onPlayErrorNoError();

        void onPlayErrorWaitForFetchHistoryCompleted();

        void onSafeProtectionChanged(boolean safeProtectionOpened);

        void onStreamModeChanged(int mode);

        void onHistoryCheckerErrorNoSDCard();

        void onHistoryCheckerErrorSDCardInitRequired(int errorCode);

        void onLoadHistoryPrepared(long playTime, boolean isHistoryCheckerRequired);

        void onVideoPlayTypeChanged(boolean isLive);

        void onDeviceMotionChanged(boolean motionAreaEnabled, DpMsgDefine.Rect4F motionArea);
    }

    interface Presenter extends BasePresenter {

        void performCheckVideoPlayError();

        void performStopVideoAction(boolean notify);

        void performPlayVideoAction(boolean live, long timestamp);

        void performPlayVideoAction();

        void performHistoryPlayAndCheckerAction(long playTime);

        void performLivePictureCaptureSaveAction(boolean saveInPhotoAndNotify);

        void performChangeSpeakerAction(boolean on);

        void performChangeSpeakerAction();

        void performChangeMicrophoneAction(boolean on);

        void performChangeMicrophoneAction();

        void performChangeStreamModeAction(int mode);

        void performLoadLiveThumbPicture();

        void performResetFirstSight();

        void performLocalNetworkPingAction();

        void performChangeSafeProtection(int event);//event,0:unchecked,1:checked safe sdcard

        void performViewModeChecker(int displayMode);

        boolean isStandBy();

        String getUuid();

        /**
         * 修改摄像头配置属性
         *
         * @param value
         * @param id
         */
        <T extends DataPoint> void updateInfoReq(T value, long id);

        float getVideoPortHeightRatio(boolean isLand);

        boolean isEarpiecePlug();

        void switchEarpiece(boolean s);

        void fetchHistoryDataListV1(String uuid, long playTime);

        void fetchHistoryDataListV2(String uuid, int time, int way, int count, long playTime);

        void openDoorLock(String password);

        boolean isHistoryEmpty();

        boolean isLive();

        boolean isLivePlaying();

        boolean isLoading();

        boolean canShowLoadingBar();

        boolean canHideLoadingBar();

        boolean canShowViewModeMenu();

        boolean canShowStreamSwitcher();

        boolean canShowXunHuan();

        boolean canShowHistoryWheel();

        void performLiveMotionAreaCheckerAction(boolean toggleMotionAreaSetting);

        boolean canShowFlip();

        boolean canShowFirstSight();

        boolean canDoorLockEnable();

        boolean canShowHistoryCase();

        boolean canLoadHistoryEnable();

        boolean canModeSwitchEnable();

        boolean canXunHuanEnable();

        boolean canPlayVideoNow();

        boolean canShowDoorLock();

        boolean canShowMicrophone();

        boolean canShowSpeaker();

        boolean canStreamSwitcherEnable();

        boolean isVideoLoading();

        boolean canCaptureEnable();

        boolean canMicrophoneEnable();

        boolean canSpeakerEnable();

        boolean isNoPlayError();

        int getDisplayMode();

        int getMountMode();

        CameraParam getCameraParam();

        boolean canHideStreamSwitcher();

        boolean canHideViewModeMenu();

        boolean isSafeProtectionOpened();

        int getStreamMode();

        boolean canShowMotionArea();

        DpMsgDefine.Rect4F getMotionArea();

        boolean canMotionAreaEnable();

        boolean canShowMotionAreaOption();

        boolean shouldReloadLiveThumbPicture();
    }
}

