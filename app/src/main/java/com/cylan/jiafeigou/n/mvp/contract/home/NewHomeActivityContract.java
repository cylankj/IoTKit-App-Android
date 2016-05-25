package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by hunt on 16-5-23.
 */
public interface NewHomeActivityContract {

    interface View extends BaseView<Presenter> {
        void initView();
    }

    interface Presenter extends BasePresenter {
    }
}
