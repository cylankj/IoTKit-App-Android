package com.cylan.jiafeigou.n.mvp.contract.home;

import android.support.annotation.UiThread;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.GreetBean;

import java.util.List;

/**
 * Created by hunt on 16-5-23.
 */
public interface HomePageListContract {

    interface View extends BaseView<Presenter> {
        @UiThread
        void onDeviceListRsp(List<DeviceBean> resultList);


        /**
         * @param greetBean: 从presenter处理后返回.
         */
        @UiThread
        void onGreetUpdate(GreetBean greetBean);

        /**
         * @param dayTime：0白天 1黑夜
         */
        void onTimeTick(int dayTime);

        /**
         * @param state login:{@link com.cylan.jiafeigou.misc.JFGRules#LOGIN}
         *              logout :{@link com.cylan.jiafeigou.misc.JFGRules#LOGOUT}
         */
        void onLoginState(int state);
    }

    interface Presenter extends BasePresenter {
        void fetchGreet();

        void fetchDeviceList();

        void deleteItem(DeviceBean deviceBean);

        void registerWorker();

        void unRegisterWorker();
    }
}
