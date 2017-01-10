package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public interface MineFriendDetailContract {

    interface View extends BaseView<Presenter> {
        /**
         * 处理删除的回调
         */
        void handlerDelCallBack(int code);

        /**
         * 显示删除进度
         */
        void showDeleteProgress();

        /**
         * 隐藏删除进度
         */
        void hideDeleteProgress();

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);
    }

    interface Presenter extends BasePresenter {
        /**
         * 发送删除好友请求
         *
         * @param account
         */
        void sendDeleteFriendReq(String account);


        /**
         * 注册网络监听
         */
        void registerNetworkMonitor();

        /**
         * 移除网络监听
         */
        void unregisterNetworkMonitor();

        /**
         * 删除好友度的回调
         * @return
         */
        Subscription delFriendBack();
    }
}
