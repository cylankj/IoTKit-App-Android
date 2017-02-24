package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.base.view.CallablePresenter;
import com.cylan.jiafeigou.base.view.CallableView;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellLiveContract {

    interface View extends CallableView {

        void onLoginState(int state);

        void onLiveStop(int errId);



    }

    interface Presenter extends CallablePresenter {

        void capture();
    }
}

