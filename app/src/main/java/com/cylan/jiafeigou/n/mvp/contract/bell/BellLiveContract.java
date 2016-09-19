package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellLiveContract {


    interface View extends BaseView<Presenter> {


        void onLoginState(int state);

    }

    interface Presenter extends BasePresenter {

        /**
         * 接听
         */
        void onPickup();

        /**
         * 挂断
         */
        void onDismiss();

        void onMike(int on);

        void onCapture();
    }
}

