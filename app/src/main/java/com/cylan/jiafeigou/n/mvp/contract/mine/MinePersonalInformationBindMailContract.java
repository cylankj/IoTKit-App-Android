package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/12
 * 描述：
 */
public interface MinePersonalInformationBindMailContract {

    interface View extends BaseView<Presenter> {
        void showMailHasBindDialog();                   //显示邮箱已经绑定
    }

    interface Presenter extends BasePresenter{
        boolean checkEmail(String email);               //检查邮箱的合法性
        boolean checkEmailIsBinded(String email);       //检验邮箱是否已经绑定过
    }
}
