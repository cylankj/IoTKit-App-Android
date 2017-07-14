package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/12
 * 描述：
 */
public interface BindMailContract {

    interface View extends BaseView<Presenter> {

        /**
         * 显示请求发送之后的对话框
         */
        void showSendReqResult(int code);

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);

        void showLoading(int resId, Object... args);

        void hideLoading();

        void onAccountArrived(JFGAccount jfgAccount);
    }

    interface Presenter extends BasePresenter {

        boolean checkEmail(String email);               //检查邮箱的合法性

        /**
         * 发送修改用户属性请求
         */
        void sendSetAccountReq(String newEmail);

        /**
         * 拿到用户的账号
         *
         * @return
         */
        Account getUserAccount();

        /**
         * 获取是否三方登录
         *
         * @return
         */
        boolean isOpenLogin();
    }
}
