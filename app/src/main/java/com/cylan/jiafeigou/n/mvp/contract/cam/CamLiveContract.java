package com.cylan.jiafeigou.n.mvp.contract.cam;

import android.graphics.Bitmap;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.n.mvp.BaseFragmentView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.widget.wheel.ex.IData;

import java.io.IOException;
import java.util.ArrayList;

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

        void onHistoryDataRsp(IData dataProvider);

        void onRtcp(JFGMsgVideoRtcp rtcp, boolean ignoreTimeStamp);

        void onResolution(JFGMsgVideoResolution resolution) throws JfgException;

//        void onDeviceInfoChanged(long id);

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

//        /**
//         * 保存了每一份数据的第一条的时间戳
//         *
//         * @param dateList
//         */
//        void onHistoryDateListUpdate(ArrayList<Long> dateList);

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

        /**
         * 抓取历史录像列表
         */
//        void fetchHistoryDataList();

        boolean isShareDevice();

//        void setStopReason(int stopReason);

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
         * 预览专用？
         *
         * @param forPopWindow
         */
        void takeSnapShot(boolean forPopWindow);

        /**
         * 保存标志
         *
         * @param flag
         */
        void saveAlarmFlag(boolean flag);

        void saveAndShareBitmap(Bitmap bitmap, boolean b);

        /**
         * @return <Integer:天数,Long:时间戳>
         */
        ArrayList<Long> getFlattenDateList();

        IData getHistoryDataProvider();

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


        Observable<IData> assembleTheDay(long timeStart);

        LiveStream getLiveStream();

        void updateLiveStream(LiveStream liveStream);

        float getVideoPortHeightRatio();

        boolean isEarpiecePlug();

        void switchEarpiece(boolean s);

        void saveHotSeatState();

        void restoreHotSeatState();

        boolean isDeviceStandby();

        boolean fetchHistoryDataList();

        /**
         * 按照时间查
         *
         * @param time
         * @return
         */
        boolean fetchHistoryDataList(long time);

        void openDoorLock(String password);
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

