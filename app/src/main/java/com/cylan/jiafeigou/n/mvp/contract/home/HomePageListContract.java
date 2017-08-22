package com.cylan.jiafeigou.n.mvp.contract.home;

import android.support.annotation.UiThread;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.view.adapter.item.HomeItem;

import java.util.List;

/**
 * Created by hunt on 16-5-23.
 */
public interface HomePageListContract {

    interface View extends BaseView<Presenter> {

        @UiThread
        void onItemsRsp(List<Device> DeviceList);

        /**
         * 对单个设备操作
         *
         * @param index
         */
        void onItemUpdate(int index);

        void onItemDelete(int index);

        List<HomeItem> getUuidList();

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

        void onNetworkChanged(boolean connected);

//        void unBindDeviceRsp(int state);

        void autoLoginTip(int code);

        void clientUpdateDialog(String apkPath);

        void onRefreshDeviceList();
    }

    interface Presenter extends BasePresenter {

        void fetchDeviceList(boolean manually);

        void refreshDevices();
    }
}