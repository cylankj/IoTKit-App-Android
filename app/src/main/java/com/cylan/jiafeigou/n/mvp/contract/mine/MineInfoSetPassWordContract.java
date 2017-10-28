package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGResult;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/20
 * 描述：
 */
public interface MineInfoSetPassWordContract {

    interface View extends BaseView {

        String getOldPassword();

        String getNewPassword();

        void changePwdResult(JFGResult jfgResult);
    }

    interface Presenter extends BasePresenter {

        boolean checkOldPassword(String oldPass);

        boolean checkNewPassword(String oldPass, String newPass);

        /**
         * 检测新密码长度
         *
         * @param newPass
         * @return
         */
        boolean checkNewPasswordLength(String newPass);

        /**
         * 发送修改密码请求
         *
         * @param account
         */
        void sendChangePassReq(String account, String oldPass, String newPass);

        /**
         * 修改密码的回调
         *
         * @return
         */
        Subscription changePwdBack();
    }

}
