package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public interface MineInfoContract {

    interface View extends BaseView {

        void initPersonalInformation(Account account);          //初始化显示个人信息

    }


    interface Presenter extends BasePresenter {

        boolean checkOpenLogin();

        void monitorPersonInformation();
    }

}
