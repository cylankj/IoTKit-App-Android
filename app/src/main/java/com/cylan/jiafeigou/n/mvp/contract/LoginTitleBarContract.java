package com.cylan.jiafeigou.n.mvp.contract;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by cylan-hunt on 16-6-30.
 */
public interface LoginTitleBarContract {


    interface LoginTitleBarView extends BaseView<LoginTitleBarPresenter> {

        /**
         * @param titleContent
         * @param rightContent
         */
        void updateTitle(final String titleContent, final String rightContent);

    }

    interface LoginTitleBarPresenter extends BasePresenter {

        void decideBackAction();

    }
}
