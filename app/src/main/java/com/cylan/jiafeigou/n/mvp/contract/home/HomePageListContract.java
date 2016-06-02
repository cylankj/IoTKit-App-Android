package com.cylan.jiafeigou.n.mvp.contract.home;

import android.support.annotation.UiThread;

import com.cylan.jiafeigou.n.model.DeviceBean;
import com.cylan.jiafeigou.n.model.GreetBean;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.util.List;

/**
 * Created by hunt on 16-5-23.
 */
public interface HomePageListContract {

    interface View extends BaseView<Presenter> {
        @UiThread
        void onDeviceListRsp(List<DeviceBean> resultList);


        /**
         * @param greetBean: 从presenter处理后返回.
         */
        @UiThread
        void onGreetUpdate(GreetBean greetBean);
    }

    interface Presenter extends BasePresenter {
        void startRefresh();

        void onDeleteItem(DeviceBean deviceBean);


    }
}
