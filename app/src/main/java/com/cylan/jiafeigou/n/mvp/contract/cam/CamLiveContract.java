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

        void audioRecordPermissionDenied();

        void onBatteryDrainOut();

        void onHistoryLoadFinished();

        void onDeviceUnBind();

        void onOpenDoorError();

        void onOpenDoorSuccess();

        void onOpenDoorPasswordError();

        void onHistoryEmpty();

        void onHistoryReady(Collection<JFGVideo> history);

        void onLoadHistoryFailed();

        void onDeviceStandByChanged(boolean isStandBy);

        void onPlayErrorFirstSight();

        void onPlayErrorNoNetwork();

        void onPlayErrorDeviceOffLine();

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

        void onUpdateVideoLoading(boolean showLoading);

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
    }

    interface Presenter extends BasePresenter {
        void performStopVideoAction(boolean notify);

        void performPlayVideoAction(boolean live, long timestamp);

        void performPlayVideoAction();

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

        boolean canShowHistoryWheel();

        boolean canPlayVideoNow();

        boolean canShowFlip();

        boolean canShowFirstSight();

        boolean canDoorLockEnable();

        boolean canShowHistoryCase();


    }
}

