package com.cylan.jiafeigou.n.mvp.contract.cloud;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public interface CloudLiveDeviceInfoContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {

        int checkSdCard();                  //检查sd卡状态

        String getMobileType();             //获取运营商

        String getWifiState();              //获取WiFi的状态

    }

}
