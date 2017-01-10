package com.cylan.jiafeigou.n.mvp.contract.setting;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by hunt on 16-5-26.
 */

public interface AccountInfoContract {

    interface View extends BaseView<Presenter> {
        void initBackStackChangeListener();

        void onTitleActionChange();
    }

    interface Presenter extends BasePresenter {
        View getView();
    }
}
