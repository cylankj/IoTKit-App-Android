package com.cylan.jiafeigou.n.mvp.contract.cloud;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseDbBean;

import java.io.Serializable;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2017/1/6
 * 描述：
 */
public interface CloudLiveCallContract {

    interface View extends BaseView<Presenter> {

        void showLoadingView();

        void hideLoadingView();

        void setLoadingText(String text);

        void onLiveStop(int code);

        /**
         * 设备响应分辨率消息
         */
        void onResolution(JFGMsgVideoResolution resolution) throws JfgException;

        /**
         * 呼叫结果的处理
         */
        void handlerCallingReuslt(int msgId);

    }

    interface Presenter extends BasePresenter {

        /**
         * 呼出
         */
        void onCloudCallConnettion();

        /**
         * 分辨率的回调
         *
         * @return
         */
        Subscription resolutionNotifySub();

        /**
         * 停止播放
         */
        void stopPlayVideo();


        /**
         * 呼叫的结果
         *
         * @return
         */
        Subscription callingResult();

        /**
         * 30s计时
         */
        void countTime();

        /**
         * 保存到数据库
         */
        void saveIntoDb(CloudLiveBaseDbBean bean);

        /**
         * 获取到账号的信息用于创建数据库
         */
        Subscription getAccount();

        /**
         * 字符串转byte[]
         *
         * @param s
         * @return
         */
        byte[] getSerializedObject(Serializable s);

        /**
         * 是否连接成功
         *
         * @return
         */
        boolean getIsConnectOk();

        /**
         * 系统时间转换
         *
         * @param times
         * @return
         */
        String parseTime(long times);

        /**
         * 获取到用户的头像
         *
         * @return
         */
        String getUserIcon();

    }

}
