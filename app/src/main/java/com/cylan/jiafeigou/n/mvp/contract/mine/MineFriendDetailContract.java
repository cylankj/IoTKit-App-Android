package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

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
        void handlerDelCallBack();

        /**
         * 显示删除进度
         */
        void showDeleteProgress();

        /**
         * 隐藏删除进度
         */
        void hideDeleteProgress();
    }

    interface Presenter extends BasePresenter {
        /**
         * 发送删除好友请求
         * @param account
         */
        void sendDeleteFriendReq(String account);
    }
}
