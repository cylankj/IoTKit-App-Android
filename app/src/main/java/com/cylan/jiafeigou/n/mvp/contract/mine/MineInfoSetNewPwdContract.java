package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/12/28
 * 描述：
 */
public interface MineInfoSetNewPwdContract {
    interface View extends BaseView<Presenter> {
        void registerResult(int code);
    }

    interface Presenter extends BasePresenter{
        /**
         * 注册
         * @param mail
         * @param pwd
         */
        void openLoginRegister(String mail,String pwd,String token);

        /**
         * 注册回调
         * @return
         */
        Subscription registerBack();
    }
}
