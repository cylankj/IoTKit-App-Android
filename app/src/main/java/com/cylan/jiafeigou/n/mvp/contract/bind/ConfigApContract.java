package com.cylan.jiafeigou.n.mvp.contract.bind;

import android.net.wifi.ScanResult;

import com.cylan.jiafeigou.misc.bind.IBindUdpFlow;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.util.List;

/**
 * Created by cylan-hunt on 16-7-6.
 */
public interface ConfigApContract {


    interface View extends BaseView<Presenter> {

        /**
         * wifi状态变化
         *
         * @param state -2:unknown,-1:offline 0:mobile 1:wifi
         */
        void onNetStateChanged(int state);

        /**
         * wifi列表回调
         *
         * @param resultList
         */
        void onWiFiResult(List<ScanResult> resultList);

        void onSetWifiFinished(UdpConstant.UdpDevicePortrait udpDevicePortrait);

        /**
         * 丢失与狗的连接
         */
        void lossDogConnection();

        /**
         * {@link IBindUdpFlow#startUpgrade()}
         *
         * @param state
         */
        void upgradeDogState(int state);
        void pingFailed();
    }

    interface Presenter extends BasePresenter {

        /**
         * @param ssid
         * @param pwd
         * @param type
         */
        void sendWifiInfo(String ssid, String pwd, int type);

        /**
         * 会发送ping,fping消息,确认设备是否在通信范围内.
         */
        void checkDeviceState();

        void refreshWifiList();

        void check3GDogCase();
        /**
         * 先清空其他狗绑定的信息
         */
        void clearConnection();

//        /**
//         * 一旦进来页面,就开始ping流程,避免在后台做,ping结果需要保持,状态判断.
//         * 这一流程,非常快.
//         */
//        boolean startPingFlow();

        boolean isConnectDog();

        void finish();
    }
}
