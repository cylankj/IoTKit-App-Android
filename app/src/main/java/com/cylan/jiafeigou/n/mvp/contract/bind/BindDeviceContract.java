package com.cylan.jiafeigou.n.mvp.contract.bind;

import android.net.wifi.ScanResult;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.util.List;

/**
 * Created by cylan-hunt on 16-7-6.
 */
public interface BindDeviceContract {

    int STATE_NO_RESULT = 0;
    int STATE_HAS_RESULT = 1;
    int STATE_NO_JFG_DEVICE = 2;

    interface View extends BaseView<Presenter> {

        /**
         * 成功找到匹配的AP
         */
        void onDevicesRsp(List<ScanResult> resultList);

        /**
         * 没有wifi列表
         */
        void onNoListError();

        void onNoJFGDevices();
    }

    interface Presenter extends BasePresenter {

        /**
         * 扫描附近设备
         */
        void scanDevices(String... filters);


    }
}
