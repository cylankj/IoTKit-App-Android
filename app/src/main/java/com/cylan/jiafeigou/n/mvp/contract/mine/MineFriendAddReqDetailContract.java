package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public interface MineFriendAddReqDetailContract {

    interface View extends BaseView<Presenter> {

        /**
         * 显示或隐藏添加请求信息
         * @param isFrome
         */
        void showOrHideReqMesg(boolean isFrome);

        /**
         * 添加请求过期弹出框
         */
        void showReqOutTimeDialog();

        /**
         * 添加请求已发送的提示
         */
        void showSendAddReqResult(boolean flag);

        /**
         * 添加成功提示
         */
        void showAddedReult(boolean flag);

    }

    interface Presenter extends BasePresenter {
        /**
         * 添加为亲友
         */
        void handlerAddAsFriend(MineAddReqBean addRequestItems);

        /**
         * 判断添加请求是否过期
         * @param addRequestItems
         * @return
         */
        void checkAddReqOutTime(MineAddReqBean addRequestItems);
        /**
         * 发送添加请求
         * @param addRequestItems
         */
        void sendAddReq(MineAddReqBean addRequestItems);
    }

}
