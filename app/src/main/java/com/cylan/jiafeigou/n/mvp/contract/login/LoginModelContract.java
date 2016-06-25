package com.cylan.jiafeigou.n.mvp.contract.login;

import com.cylan.jiafeigou.n.model.LoginAccountBean;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by lxh on 16-6-24.
 */
public interface LoginModelContract {

    /**
     * 授权失败
     */
    static final int AUTHORIZE_ERROR = 2;

    /**
     * 取消授权
     */
    static final int AUTHORIZE_CANCLE = 1;

    /**
     * 授权成功
     */
    static final int AUTHORIZE_SUCCESSFUL = 0;


    interface LoginView extends BaseView<LoginPresenter> {

        /**
         * 登陆结果
         *
         * @param login , 只需要关注BeanInfoLogin 中的ret 和session . ret 为0才有session
         */
        void loginResult(LoginAccountBean login);


        /**
         * 新浪授权的结果
         *
         * @param ret 0为成功，1为用户取消授权，2为授权失败。
         */
        void onQQAuthorizeResult(int ret);

        /**
         * 新浪授权的结果
         *
         * @param ret 0为成功，1为用户取消授权，2为授权失败。
         */
        void onSinaAuthorizeResult(int ret);
    }

    interface LoginPresenter extends BasePresenter {
        /**
         * 执行登陆
         *
         * @param info
         */
        void executeLogin(LoginAccountBean info);

        /**
         * 获取QQ的授权
         */
        void getQQAuthorize();

        /**
         * 获取新浪微博的授权
         */
        void getSinaAuthorize();
    }


}
