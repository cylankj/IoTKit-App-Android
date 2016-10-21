package com.cylan.jiafeigou.n.mvp.contract.cloud;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BellInfoBean;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/29
 * 描述：
 */
public interface CloudCorrelationDoorBellContract {

    interface View extends BaseView<Presenter> {
        void initRecycleView(List<BellInfoBean> list);

        void showNoRelativeDevicesView();                       //显示没有设备图
    }

    interface Presenter extends BasePresenter {
        void loadDoorBellData(String url);
    }

}
