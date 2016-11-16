package com.cylan.jiafeigou.n.mvp.contract.bind;

import android.content.Context;
import android.net.wifi.ScanResult;

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
        void onWifiStateChanged(int state);

        /**
         * wifi列表回调
         *
         * @param resultList
         */
        void onWiFiResult(List<ScanResult> resultList);

    }

    interface Presenter extends BasePresenter {

        /**
         * 注册wifi广播
         *
         * @param context : this context should be application context
         */
        void registerWiFiBroadcast(Context context);
    }
}
