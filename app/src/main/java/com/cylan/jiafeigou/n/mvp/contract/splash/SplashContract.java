package com.cylan.jiafeigou.n.mvp.contract.splash;

import com.cylan.jiafeigou.ads.AdsStrategy;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Observable;

/**
 * Created by hunt on 16-5-23.
 */
public interface SplashContract {

    interface View extends BaseView {
        void loginSuccess();

        void loginError(int code);
    }

    interface Presenter extends BasePresenter {
        void autoLogin();

        void selectNext(boolean showSplash);

        Observable<AdsStrategy.AdsDescription> showAds();

        void reEnableSmartcallLog();

        void startInitialization();
    }

}
