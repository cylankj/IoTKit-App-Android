package com.cylan.jiafeigou.n.mvp.contract.cam;

import android.graphics.Bitmap;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.module.CameraLiveActionHelper;
import com.cylan.jiafeigou.module.DPTimeZone;
import com.cylan.jiafeigou.n.mvp.BaseFragmentView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;

import java.io.IOException;
import java.util.Collection;

import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_STOP;

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

        void onDeviceInfoChanged(JFGDPMsg msg) throws IOException;

        void showFirmwareDialog();

        void audioRecordPermissionDenied();

        boolean isUserVisible();

        void onBatteryDrainOut();

        void onHistoryLoadFinished();

        void onDeviceUnBind();

        void onOpenDoorError();

        void onOpenDoorSuccess();

        void onOpenDoorPasswordError();

        void onHistoryEmpty();

        void onHistoryReady(Collection<JFGVideo> history);

        void onLoadHistoryFailed();

        void onPlayErrorStandBy();

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

        void onDeviceTimeZoneChanged(DPTimeZone timeZone);

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
    }

    interface Presenter extends BasePresenter {

        void performStopVideoAction(boolean live);

        void performStopVideoAction();

        void performPlayVideoAction(boolean live, long timestamp);

        void performPlayVideoAction();

        void performLivePictureCaptureSaveAction(boolean saveInPhotoAndNotify);

        void performChangeSpeakerAction(boolean on);

        void performChangeSpeakerAction();

        void performChangeMicrophoneAction(boolean on);

        void performChangeMicrophoneAction();

        void performChangeStreamModeAction(int mode);

        void performLoadLiveThumbPicture();

        CameraLiveActionHelper getCameraLiveAction();

        String getUuid();

        /**
         * 修改摄像头配置属性
         *
         * @param value
         * @param id
         */
        <T extends DataPoint> void updateInfoReq(T value, long id);

        float getVideoPortHeightRatio();

        boolean isEarpiecePlug();

        void switchEarpiece(boolean s);

        void fetchHistoryDataListV1(String uuid);

        void fetchHistoryDataListV2(String uuid, int time, int way, int count);

        void openDoorLock(String password);

        boolean isHistoryEmpty();
    }

    class LiveStream {
        public int type = TYPE_LIVE;
        public long time = -1;
        public int playState = PLAY_STATE_STOP;

        public volatile long playStartTime = 0;

        @Override
        public String toString() {
            return "LiveStream{" +
                    "type=" + type +
                    ", time=" + time +
                    ", playState=" + playState +
                    '}';
        }
    }
}

