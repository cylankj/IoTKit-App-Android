package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.util.List;

/**
 * Created by hunt on 16-5-23.
 */
public interface HomePageListContract {

    interface View extends BaseView<Presenter> {

        void onDeviceListRsp(List<DeviceBean> resultList);


    }

    interface Presenter extends BasePresenter {
        void startRefresh();

        void onDeleteItem(DeviceBean deviceBean);
    }
}
