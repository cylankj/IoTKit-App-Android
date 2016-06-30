package com.cylan.jiafeigou.n.mvp.contract.login;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;

import org.msgpack.annotation.NotNullable;

/**
 * Created by chen on 5/30/16.
 */
public interface SetupPwdContract {


    interface SetupPwdView extends BaseView<SetupPwdPresenter> {

        /**
         * 登陆结果
         *
         * @param bean , 返回结果。
         */
        void submitResult(RequestResetPwdBean bean);

    }

    interface SetupPwdPresenter extends BasePresenter {

        void submitAccountInfo(final String account, final String pwd, final String code);

    }
}
