package com.cylan.jiafeigou.n.mvp.contract.bind;

import android.net.wifi.ScanResult;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.util.List;

/**
 * Created by cylan-hunt on 16-7-6.
 */
public interface BindDeviceContract {


    interface View extends BaseView<Presenter> {

        /**
         * 成功找到匹配的AP
         */
        void onDevicesRsp(List<ScanResult> resultList);
    }

    interface Presenter extends BasePresenter {

        /**
         * 扫描附近设备
         */
        void scanDevices();



    }
}
