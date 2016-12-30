package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.JFGPresenter;
import com.cylan.jiafeigou.base.JFGView;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellLiveContract {


    interface View extends JFGView {


        void onLoginState(int state);


        /**
         * 设备响应分辨率消息
         */
        void onResolution(JFGMsgVideoResolution resolution) throws JfgException;

        /**
         * 更新流量信息
         */
        void onFlowSpeed(int speed);

        /**
         * 直播或者历史录像
         *
         * @param errId : 失败 0:网络失败
         */
        void onLiveStop(int errId);

        /**
         * 新的门铃呼叫到来,且现在没有已接听的门铃
         */
        void onListen();

        /**
         * 显示门铃预览图
         */
        void onPreviewPicture(String URL);

        /**
         * 客户端主动查看门铃
         */
        void onViewer();

        /**
         * 设置当前麦克风状态
         */
        void onSpeaker(boolean on);
    }

    interface Presenter extends JFGPresenter {

        /**
         * 接听
         */
        void onPickup();

        /**
         * 挂断
         */
        void onDismiss();

        /**
         * 切换speaker
         */
        void onSwitchSpeaker();

        /**
         * 截屏
         */
        void onCapture();

        BeanBellInfo getBellInfo();

        /**
         * 当view创建的时候会调用这个方法,以后会优化
         */
        void onBellCall(String callWay, Object extra, Object extra1);

        void onScreenRotationChanged(boolean land);
    }
}

