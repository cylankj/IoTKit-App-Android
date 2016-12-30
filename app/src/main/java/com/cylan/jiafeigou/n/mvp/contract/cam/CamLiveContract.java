package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.widget.wheel.ex.IData;

import java.util.Map;

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


        void onHistoryDataRsp(IData dataProvider);

        void onRtcp(JFGMsgVideoRtcp rtcp);

        void onResolution(JFGMsgVideoResolution resolution) throws JfgException;

        /**
         * @param state
         */
        void onDeviceStandBy(boolean state);

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

        void onTakeSnapShot(boolean state);

        void onBeanInfoUpdate(BeanCamInfo info);

        /**
         * 历史录像播放结束状态
         *
         * @param state
         */
        void onHistoryLiveStop(int state);
    }

    interface Presenter extends BasePresenter {

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
        void fetchHistoryDataList();

        boolean isShareDevice();

        /**
         * 开始播放历史录像或者开始直播
         */
        void startPlayVideo(int type);

        /**
         * 开始播放历史录像
         *
         * @param time
         */
        void startPlayHistory(long time);

        /**
         * 停止播放历史录像或者直播
         *
         * @param type
         */
        void stopPlayVideo(int type);

        BeanCamInfo getCamInfo();

        String getUuid();

        /**
         * @param local       :true:加菲狗客户端,false:设备端
         * @param speakerFlag :true:开 false:关
         * @param micFlag     :true:开 false:关
         */
        void switchSpeakerMic(final boolean local, final boolean speakerFlag, final boolean micFlag);

        void takeSnapShot();

        boolean getSpeakerFlag();

        boolean getMicFlag();

        /**
         * 保存标志
         *
         * @param flag
         */
        void saveAlarmFlag(boolean flag);

        /**
         * @return <Integer:天数,Long:时间戳>
         */
        Map<Long, Long> getFlattenDateMap();

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
        void updateInfoReq(Object value, long id);
    }
}

