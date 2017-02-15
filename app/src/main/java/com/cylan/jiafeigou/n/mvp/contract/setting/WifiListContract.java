package com.cylan.jiafeigou.n.mvp.contract.setting;

import android.net.wifi.ScanResult;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 17-2-12.
 */

public interface WifiListContract {

    interface View extends BaseView<Presenter> {
        void onResults(ArrayList<ScanResult> results);

        void onErr(int err);
    }

    interface Presenter extends BasePresenter {
        void startScan();

        void sendWifiInfo(String ssid, String pwd,int security);
    }
}
