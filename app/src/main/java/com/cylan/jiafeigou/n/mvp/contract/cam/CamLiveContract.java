package com.cylan.jiafeigou.n.mvp.contract.cam;

import android.graphics.Bitmap;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DataPoint;
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

        boolean isLocalMicOn();

        boolean isLocalSpeakerOn();

        boolean judge();

        void onRtcp(JFGMsgVideoRtcp rtcp);

        void onResolution(JFGMsgVideoResolution resolution) throws JfgException;

        void onDeviceInfoChanged(JFGDPMsg msg) throws IOException;

        /**
         * 准备播放
         */
        void onLivePrepare(int type);

        /**
         * 直播或者历史录像
         *
         * @param type
         */
        void onLiveStarted(int type);

        /**
         * 直播或者历史录像
         *
         * @param type
         * @param errId : 失败 0:网络失败
         */
        void onLiveStop(int type, int errId);

        void onTakeSnapShot(Bitmap bitmap);

        void onPreviewResourceReady(Bitmap bitmap);

        /**
         * 历史录像播放结束状态
         *
         * @param state
         */
        void onHistoryLiveStop(int state);

        /**
         * @param start :开始显示loading
         */
        void shouldWaitFor(boolean start);

        void showFirmwareDialog();

        void audioRecordPermissionDenied();

        void onNetworkChanged(boolean connected);

        boolean isUserVisible();

        /**
         * speakerOn,micOn,captureOn
         */
        void switchHotSeat(boolean speakerOn,
                           boolean speakerEnable,
                           boolean micOn,
                           boolean micEnable,
                           boolean captureOn,
                           boolean captureEnable);

        void onAudioPermissionCheck();

        void onBatteryDrainOut();

        void onHistoryLoadFinished();

        void onDeviceUnBind();

        void onOpenDoorError();

        void onOpenDoorSuccess();

        void onOpenDoorPasswordError();

        void onHistoryEmpty();

        void onHistoryReady(Collection<JFGVideo> history);

        void onLoadHistoryFailed();
    }

    interface Presenter extends BasePresenter {

        /**
         * sd卡中的路径
         *
         * @return
         */
        String getThumbnailKey();

        /**
         * 播放状态
         *
         * @return
         */
        int getPlayState();

        /**
         * 当前直播,或者历史视频
         *
         * @return
         */
        int getPlayType();

        boolean isShareDevice();

        /**
         * 开始播放历史录像或者开始直播
         */
        void startPlay();

        /**
         * 开始播放历史录像
         *
         * @param time
         */
        void startPlayHistory(long time);

        /**
         * 停止播放历史录像或者直播
         *
         * @param reasonOrState
         */
        Observable<Boolean> stopPlayVideo(int reasonOrState);

        /**
         * 退出页面
         *
         * @param detach
         */
        Observable<Boolean> stopPlayVideo(boolean detach);

        String getUuid();

        /**
         */
        void switchSpeaker();

        void switchMic();

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

        LiveStream getLiveStream();

        void updateLiveStream(LiveStream liveStream);

        float getVideoPortHeightRatio();

        boolean isEarpiecePlug();

        void switchEarpiece(boolean s);

        void saveHotSeatState();

        void restoreHotSeatState();

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

