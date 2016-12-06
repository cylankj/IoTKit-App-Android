package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellSettingContract {


    interface View extends BaseView<Presenter> {

        void onSettingInfoRsp(BeanBellInfo bellInfoBean);

        /**
         * 账号登录状态
         *
         * @param state
         */
        void onLoginState(boolean state);

        /**
         * 解绑设备回调
         *
         * @param state
         */
        void unbindDeviceRsp(int state);

    }

    interface Presenter extends BasePresenter {
        BeanBellInfo getBellInfo();


        /**
         * 解绑设备
         */
        void unbindDevice();
    }
}

