package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.widget.wheel.SDataStack;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface CamLiveContract {

    /**
     * 默认直播
     */
    int TYPE_LIVE = 0;
    int TYPE_HISTURY = 1;

    interface View extends BaseView<Presenter> {


        void onHistoryDataRsp(SDataStack timeSet);


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

        /**
         * 开始播放历史录像或者开始直播
         */
        void startPlayVideo(int type);

        /**
         * 停止播放历史录像或者直播
         *
         * @param type
         */
        void stopPlayVideo(int type);

        BeanCamInfo getCamInfo();

        void switchSpeakerMic(final boolean local, final boolean speakerFlag, final boolean micFlag);

        void takeSnapShot();

        boolean getSpeakerFlag();

        boolean getMicFlag();
    }
}

