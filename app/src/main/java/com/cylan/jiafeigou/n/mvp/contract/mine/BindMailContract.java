package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/12
 * 描述：
 */
public interface BindMailContract {

    interface View extends BaseView<Presenter> {
        /**
         * 显示邮箱已经绑定对话框
         */
        void showMailHasBindDialog();                   //显示邮箱已经绑定

        /**
         * 显示请求发送之后的对话框
         */
        void showSendReqResult(int code);


        /**
         * 显示绑定进度
         */
        void showSendReqHint();

        /**
         * 隐藏绑定进度
         */
        void hideSendReqHint();

        /**
         * 获取输入内容
         *
         * @return
         */
        String getEditText();

        /**
         * 标题修改
         *
         * @param account
         */
        void getUserAccountData(JFGAccount account);

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);

        /**
         * 跳转到邮箱验证界面
         */
        void jump2MailConnectFragment();
    }

    interface Presenter extends BasePresenter {

        boolean checkEmail(String email);               //检查邮箱的合法性

//        void isEmailBind(String email);       //检验邮箱是否已经绑定过

        /**
         * 发送修改用户属性请求
         */
        void sendSetAccountReq(String newEmail);


        /**
         * 修改属性后的回调
         */
        Subscription getAccountCallBack();

        /**
         * 拿到用户的账号
         *
         * @return
         */
        JFGAccount getUserAccount();

        /**
         * 三方登录回调
         *
         * @return
         */
        Subscription isOpenLoginBack();

        /**
         * 获取是否三方登录
         *
         * @return
         */
        boolean isOpenLogin();

        Subscription changeAccountBack();
    }
}
