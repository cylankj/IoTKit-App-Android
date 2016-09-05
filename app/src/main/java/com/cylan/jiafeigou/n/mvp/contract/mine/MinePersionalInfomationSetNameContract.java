package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public interface MinePersionalInfomationSetNameContract {

    interface View extends BaseView<Presenter>{
        String getEditName();
        void setTitleBarName();
    }

    interface Presenter extends BasePresenter{
        void saveName();
        boolean isEditEmpty(String string);
    }
}
