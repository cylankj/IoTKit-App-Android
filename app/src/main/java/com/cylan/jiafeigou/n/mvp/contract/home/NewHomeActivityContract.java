package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by hunt on 16-5-23.
 */
@Deprecated
public interface NewHomeActivityContract {
    @Deprecated
    interface View extends BaseView<Presenter> {
        void initView();

        //void ok();

        //void onBackPress();
    }

    @Deprecated
    public interface Presenter extends BasePresenter {
        //void loadTable();

        //void doSample();

    }
}
