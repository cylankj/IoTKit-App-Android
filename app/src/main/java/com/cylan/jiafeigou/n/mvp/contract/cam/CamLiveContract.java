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

import rx.Observable;

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


        void onTakeSnapShot(Bitmap bitmap);

        void showFirmwareDialog();

        void audioRecordPermissionDenied();

        void onNetworkChanged(boolean connected);

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
    }

    interface Presenter extends BasePresenter {

        void performStopVideoAction(boolean live);

        void performStopVideoAction();

        void performPlayVideoAction(boolean live, long timestamp);

        void performPlayVideoAction();

        void performLiveThumbSaveAction(boolean sync);

        CameraLiveActionHelper getCameraLiveAction();

        void performChangeSpeakerAction(boolean on);

        void performChangeMicrophoneAction(boolean on);

        /**
         * sd卡中的路径
         *
         * @return
         */
        String getThumbnailKey();


        boolean isShareDevice();

        String getUuid();

        Observable<Boolean> switchStreamMode(int mode);

        /**
         * 保存标志
         *
         * @param flag
         */
        void saveAlarmFlag(boolean flag);

        void saveAndShareBitmap(Bitmap bitmap, boolean b, boolean save);

        /**
         * //默认隐藏.没网络时候,也不显示,设备离线也不显示
         *
         * @return
         */
        boolean needShowHistoryWheelView();

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

//        void saveHotSeatState();
//
//        void restoreHotSeatState();

        boolean isDeviceStandby();

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

