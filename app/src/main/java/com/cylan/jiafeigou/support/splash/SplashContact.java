package com.cylan.jiafeigou.support.splash;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/**
 * Created by yanzhendong on 2017/7/5.
 */

public class SplashContact {

    public interface View extends JFGView {

        void onEnterLoginActivity();

        void onExitApp();
    }

    public interface Presenter extends JFGPresenter<View> {

        void initPermissions();

    }
}
