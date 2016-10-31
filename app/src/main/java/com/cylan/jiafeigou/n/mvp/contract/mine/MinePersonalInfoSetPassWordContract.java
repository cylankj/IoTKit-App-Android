package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/20
 * 描述：
 */
public interface MinePersonalInfoSetPassWordContract {

    interface View extends BaseView<Presenter> {
        String getOldPassword();

        String getNewPassword();
    }

    interface Presenter extends BasePresenter {
        boolean checkOldPassword(String oldPass);

        boolean checkNewPassword(String oldPass, String newPass);

        boolean checkNewPasswordLength(String newPass);
    }

}
