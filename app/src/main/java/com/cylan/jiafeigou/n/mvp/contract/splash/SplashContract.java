package com.cylan.jiafeigou.n.mvp.contract.splash;

import com.cylan.jiafeigou.ads.AdsStrategy;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by hunt on 16-5-23.
 */
public interface SplashContract {

    interface View extends BaseView {

        void onShowAdvert(AdsStrategy.AdsDescription adsDescription);

        void onAdvertOver();

        void onAutoLoginFailed();

        void onPasswordChanged();

        void onAutoLoginSuccess();
    }

    interface Presenter extends BasePresenter {

        void deciderShowAdvert();

        void performAutoLogin();
    }

}
