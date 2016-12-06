package com.cylan.jiafeigou.n.mvp.contract.mag;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanMagInfo;

/**
 * 作者：zsl
 * 创建时间：2016/9/19
 * 描述：
 */
public interface MagLiveInformationContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {

        int checkSdCard();                  //检查sd卡状态

        String getMobileType();             //获取运营商

        String getWifiState();              //获取WiFi的状态

        BeanMagInfo getMagInfoBean();

        void saveMagInfoBean(BeanMagInfo info, int id);

    }

}
