package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;

/**
 * 作者：zsl
 * 创建时间：2016/9/23
 * 描述：
 */
public interface MineSetRemarkNameContract {

    interface View extends BaseView<Presenter> {
        String getEditName();

        /**
         * 初始化页面显示
         */
        void initViewShow();

        /**
         * 设置修改完成结果
         */
        void showFinishResult();

        /**
         * 显示正在修改的进度提示
         */
        void showSendReqPro();

        /**
         * 隐藏正在修改的进度提示
         */
        void hideSendReqPro();
    }

    interface Presenter extends BasePresenter {

        boolean isEditEmpty(String string);

        /**
         * 发送修改备注名的请求
         * @param friendBean
         */
        void sendSetmarkNameReq(String newName,RelAndFriendBean friendBean);
    }
}
