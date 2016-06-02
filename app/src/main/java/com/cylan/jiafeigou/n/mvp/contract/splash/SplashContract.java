package com.cylan.jiafeigou.n.mvp.contract.splash;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by hunt on 16-5-23.
 */
public interface SplashContract {


    interface View extends BaseView<Presenter> {
    }

    interface Presenter extends BasePresenter {

    }

    interface ViewRequiredOps {
        void  timeSplashed();

        void finishDelayed();
    }
    interface PresenterOps {
        void splashTime();

        void finishAppDelay();
    }
    interface PresenterRequiredOps {
        void onTimeSplashed();

        void onFinishDelayed();
    }
}
