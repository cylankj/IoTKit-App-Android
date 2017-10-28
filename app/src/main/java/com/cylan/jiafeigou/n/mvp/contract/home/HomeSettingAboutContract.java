package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public interface HomeSettingAboutContract {

    interface View extends BaseView {

        String getHotPhone();

    }

    interface Presenter extends BasePresenter {
        void callHotPhone(String phone);
    }

}
