package com.cylan.jiafeigou.n.mvp.contract.home;

import android.support.annotation.UiThread;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;

import java.util.List;

/**
 * Created by hunt on 16-5-23.
 */
public interface HomePageListContract {

    interface View extends BaseView<Presenter> {

        @UiThread
        void onItemsInsert(List<DeviceBean> resultList);

        /**
         * 对单个设备操作
         *
         * @param index
         */
        void onItemUpdate(int index);

        void onItemDelete(int index);

        List<DeviceBean> getDeviceList();

        /**
         * @param greetBean: 从presenter处理后返回.
         */
        @UiThread
        void onAccountUpdate(JFGAccount greetBean);

        /**
         * @param dayTime：0白天 1黑夜
         */
        void onTimeTick(int dayTime);

        /**
         * @param state
         */
        void onLoginState(boolean state);

        void onRefreshFinish();
    }

    interface Presenter extends BasePresenter {
        void fetchGreet();

        void fetchDeviceList();

        void deleteItem(DeviceBean deviceBean);

        void registerWorker();

        void unRegisterWorker();
    }
}
