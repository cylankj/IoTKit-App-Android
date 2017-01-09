package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/11/7
 * 描述：
 */
public interface MineClipImageContract {

    interface View extends BaseView<Presenter> {

        /**
         * 显示上传的进度
         */
        void showUpLoadPro();

        /**
         * 隐藏上传的进度
         */
        void hideUpLoadPro();

        /**
         * 上传的结果
         *
         * @param code
         */
        void upLoadResultView(int code);


        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);

    }

    interface Presenter extends BasePresenter {

        /**
         * 上传用户头像
         *
         * @param path
         */
        void upLoadUserHeadImag(String path);

        /**
         * 接收上传的回调
         */
        Subscription getUpLoadResult();

        /**
         * 获取到用户的信息
         */
        Subscription getAccount();


        /**
         * 注册网络监听
         */
        void registerNetworkMonitor();

        /**
         * 移除网络监听
         */
        void unregisterNetworkMonitor();
    }
}
