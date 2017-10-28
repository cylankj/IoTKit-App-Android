package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.rx.RxEvent;

/**
 * 作者：zsl
 * 创建时间：2016/9/23
 * 描述：
 */
public interface MineSetRemarkNameContract {

    interface View extends BaseView {
        String getEditName();

        /**
         * 初始化页面显示
         */
        void initViewShow();

        /**
         * 设置修改完成结果
         */
        void showFinishResult(RxEvent.SetFriendMarkNameBack getFriendInfoCall);

        /**
         * 显示正在修改的进度提示
         */
        void showSendReqPro();

        /**
         * 隐藏正在修改的进度提示
         */
        void hideSendReqPro();

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);
    }

    interface Presenter extends BasePresenter {

        boolean isEditEmpty(String string);

        /**
         * 发送修改备注名的请求
         */
        void setMarkName(String newName, FriendContextItem friendContextItem);

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
