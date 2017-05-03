package com.cylan.jiafeigou.n.mvp.contract.cam;

import android.graphics.Bitmap;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.widget.wheel.ex.IData;

import java.io.IOException;
import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;

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


    interface View extends BaseView<Presenter> {

        boolean isLocalMicOn();

        boolean isLocalSpeakerOn();

        void onHistoryDataRsp(IData dataProvider);

        void onRtcp(JFGMsgVideoRtcp rtcp);

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

//        void countdownFinish();

        void hardwareResult(RxEvent.CheckDevVersionRsp rsp);

        /**
         * 保存了每一份数据的第一条的时间戳
         *
         * @param dateList
         */
        void onHistoryDateListUpdate(ArrayList<Long> dateList);

        void audioRecordPermissionDenied();

        void onNetworkChanged(boolean connected);

        boolean isUserVisible();
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
        Observable<Boolean> switchSpeaker();

        Observable<Boolean> switchMic();

        /**
         * 获取本地，远端mic Speaker标志 xxxx
         * 本地mic，本地Speaker，远端mic，远端speaker
         * 从view的icon来判断，更不容易出bug。
         *
         * @return
         */
        int getLocalMicSpeakerBit();

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
        public <T extends DataPoint> void updateInfoReq(T value, long id);

//        void startCountForDismissPop();

        /**
         * 每天检测一次新固件
         */
        void checkNewHardWare();

        Subscription checkNewHardWareBack();

        /**
         * 某一天的凌晨时间戳
         *
         * @param timeStartInSecond
         * @return
         */
        Observable<IData> assembleTheDay(long timeStartInSecond);

        PrePlayType getPrePlayType();

        float getVideoPortHeightRatio();
    }

    class PrePlayType {
        public int type = TYPE_LIVE;
        public long time;
        public int playState;

        @Override
        public String toString() {
            return "PrePlayType{" +
                    "type=" + type +
                    ", time=" + time +
                    ", playState=" + playState +
                    '}';
        }
    }
}

