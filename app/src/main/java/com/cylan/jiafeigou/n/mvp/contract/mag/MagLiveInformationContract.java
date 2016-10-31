package com.cylan.jiafeigou.n.mvp.contract.mag;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/19
 * 描述：
 */
public interface MagLiveInformationContract {

    interface View extends BaseView<Presenter> {
        void initSdCardState(int state);        //初始化显示sd的状态
    }

    interface Presenter extends BasePresenter {
        int checkSdCard();                  //检查sd卡状态

        String getMobileType();             //获取运营商

        String getWifiState();              //获取WiFi的状态
    }

}
