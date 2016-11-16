package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BellInfoBean;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellSettingContract {


    interface View extends BaseView<Presenter> {

        void onSettingInfoRsp(BellInfoBean bellInfoBean);

        void onLoginState(boolean state);

    }

    interface Presenter extends BasePresenter {

        void sendActivityResult(RxEvent.ActivityResult result);
    }
}

