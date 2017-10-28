package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.rx.RxEvent;

/**
 * 作者：zsl personal
 * 创建时间：2016/9/1
 * 描述：
 */
public interface MineBindPhoneContract {

    interface View extends BaseView {

        void onResult(int event, int errId);


        /**
         * 修改标题
         */
        void initToolbarTitle(String title);

        /**
         * 获取到输入号码
         *
         * @return
         */
        String getInputPhone();

        /**
         * 获取到输入的验证码
         *
         * @return
         */
        String getInputCheckCode();

        /**
         * 检测账号的是否已经注册的结果
         *
         * @param registerBack
         */
        void handlerCheckPhoneResult(RxEvent.CheckRegisterBack registerBack);

        /**
         * 校验短信验证码的结果
         */
        void handlerCheckCodeResult(RxEvent.ResultVerifyCode resultVerifyCode);

        /**
         * 处理修改结果
         */
        void handlerResetPhoneResult(int code);

        /**
         * 显示loading
         */
        void showLoadingDialog();

        /**
         * 隐藏loading
         */
        void hideLoadingDialog();

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);

        /**
         * 获取验证码的结果
         */
        void getSmsCodeResult(int code);

    }

    interface Presenter extends BasePresenter {

        void getVerifyCode(String phone);

        /**
         * 判断是绑定还是修改
         *
         * @param userinfo
         */
        void isBindOrChange(JFGAccount userinfo);


        /**
         * 校验短信验证码
         *
         * @param vCode
         */
        void CheckVerifyCode(String phone, String vCode);


        /**
         * 是否三方登录
         *
         * @return
         */
        boolean isOpenLogin();
    }

}
