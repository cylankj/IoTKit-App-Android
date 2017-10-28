package com.cylan.jiafeigou.n.mvp.contract.login;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import org.msgpack.annotation.NotNullable;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface RstPwdContract {

    interface View extends BaseView {

        /**
         * 登陆结果
         *
         * @param ret , 返回结果。
         */
        void submitResult(int ret);

    }

    interface Presenter extends BasePresenter {
        /**
         * 提交新账号
         *
         * @param account
         */
        void executeSubmitNewPwd(final String account, @NotNullable String pwdInMd5);

    }
}
