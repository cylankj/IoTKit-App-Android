package com.cylan.jiafeigou.n.mvp.contract.cloud;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.JFGPresenter;
import com.cylan.jiafeigou.base.JFGView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2017/1/6
 * 描述：
 */
public interface CloudLiveCallContract {

    interface View extends BaseView<Presenter>{

        void showLoadingView();

        void hideLoadingView();

        void setLoadingText(String text);

        void onLiveStop(int code);
        /**
         * 设备响应分辨率消息
         */
        void onResolution(JFGMsgVideoResolution resolution) throws JfgException;

    }

    interface Presenter extends BasePresenter {

        /**
         * 呼出
         */
        void onCloudCallOut();

        /**
         * 分辨率的回调
         * @return
         */
        Subscription resolutionNotifySub();

        /**
         * 停止播放
          */
        void stopPlayVideo();
    }

}
