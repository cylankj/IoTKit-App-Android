package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public interface MineFriendSearchContract {

    interface View extends BaseView {


        /**
         * 显示查询进度
         */
        void showLoading(int resId, String... args);

        /**
         * 隐藏查询进度
         */
        void hideLoading();

        void onCheckFriendResult(FriendContextItem friendContextItem);

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);

    }

    interface Presenter extends BasePresenter {
        /**
         * 检测好友账号是否注册过
         */
        void checkFriendAccount(String account);

    }
}
