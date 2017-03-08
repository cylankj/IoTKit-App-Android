package com.cylan.jiafeigou.n.mvp.contract.splash;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * Created by hunt on 16-5-23.
 */
public interface SplashContract {


    interface View extends BaseView<Presenter> {
        /**
         * 闪屏结束
         */
        void splashOver();

        void finishDelayed();

        void loginResult(int code);
    }


    interface Presenter extends BasePresenter {

        void finishAppDelay();

    }

}
