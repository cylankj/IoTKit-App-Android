package com.cylan.jiafeigou.n.mvp.contract.login;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;

/**
 * Created by chen on 5/30/16.
 */
public interface SetupPwdContract {


    interface View extends BaseView<Presenter> {

        /**
         * 登陆结果
         *
         * @param bean , 返回结果。
         */
        void submitResult(RequestResetPwdBean bean);

    }

    interface Presenter extends BasePresenter {
        void register(final String account, final String pwd, int type, String token);
    }

}
