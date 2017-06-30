package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public interface MineFriendInformationContact {

    interface View extends BaseView<Presenter> {
        /**
         * 处理删除的回调
         */
        void onDeleteResult(int code);

        /**
         * 显示删除进度
         */
        void showLoading(int resId, String... args);

        /**
         * 隐藏删除进度
         */
        void hideLoading();

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
        void deleteFriend(String account);

        void addFriend(String account);

        int getOwnerDeviceCount();

    }
}
