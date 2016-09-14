package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public interface MinePersionalInfomationBindPhoneContract {

    interface View extends BaseView<Presenter> {
        void initToolbarTitle();
    }

    interface Presenter extends BasePresenter {

    }

}
