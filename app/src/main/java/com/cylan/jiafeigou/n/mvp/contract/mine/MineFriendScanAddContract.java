package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineFriendScanAddContract {

    interface View extends BaseView {

        void showLoading(int resId, String... args);

        void hideLoading();

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);

        void onCheckFriendAccountResult(FriendContextItem friendContextItem);
    }

    interface Presenter extends BasePresenter {

        void checkFriendAccount(String scanResult);

    }

}
