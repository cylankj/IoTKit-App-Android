package com.cylan.jiafeigou.n.mvp.contract.login;

import com.cylan.jiafeigou.n.mvp.BaseFragmentView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface ForgetPwdContract {


    interface View extends BaseFragmentView {

        void onResult(int event, int errId);

        void showLoading();

        void hideLoading();
    }

    interface Presenter extends BasePresenter {

//        /**
//         * 手机号，忘记密码，手机号+验证码
//         *
//         * @param account
//         * @param code
//         */
//        void submitPhoneNumAndCode(final String account, final String code);

//        /**
//         * 检测是否已注册
//         *
//         * @param account
//         */
//        void checkIsReg(String account);

        void getVerifyCode(String account);

        void submitPhoneAndCode(String account, String code);

        void checkMailByAccount(String mail);

        /**
         * 十分钟是否超过3次获取
         *
         * @return
         */
        boolean checkOverCount(String account);

        void submitNewPass(String pwd);

    }
}

