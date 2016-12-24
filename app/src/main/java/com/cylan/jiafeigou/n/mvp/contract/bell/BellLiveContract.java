package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellLiveContract {


    interface View extends BaseView<Presenter> {


        void onLoginState(int state);

        void onResolution(JFGMsgVideoResolution resolution) throws JfgException;

        void onFlowSpeedRefresh(int speed);

        /**
         * 直播或者历史录像
         *
         * @param errId : 失败 0:网络失败
         */
        void onLiveStop(int errId);

        //新的门铃呼叫到来,且现在没有已接听的门铃
        void onListen(String URL);

        //客户端主动查看门铃
        void onViewer();

        //新的门铃到来,且此时已有一个存在的门铃呼叫
        void onProcess(String person);
    }

    interface Presenter extends BasePresenter {

        /**
         * 接听
         */
        void onPickup();

        /**
         * 挂断
         */
        void onDismiss();

        void onMike(int on);

        void onCapture();

        BeanBellInfo getBellInfo();

        void setBellInfo(BeanBellInfo info);

        void processCall();

        void onBellCall(String callWay, Object extra, Object extra1);

        void onBellPaused();
    }
}

