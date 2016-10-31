package com.cylan.jiafeigou.n.mvp.contract.cloud;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BellInfoBean;
import com.cylan.jiafeigou.n.view.adapter.RelationDoorBellAdapter;
import com.cylan.jiafeigou.n.view.adapter.UnRelationDoorBellAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/29
 * 描述：
 */
public interface CloudCorrelationDoorBellContract {

    interface View extends BaseView<Presenter> {
        void initRelativeRecycleView(List<BellInfoBean> list);

        void initUnRelativeRecycleView(List<BellInfoBean> list);

        void showNoRelativeDevicesView(int flag);                       //显示没有关联设备文案

        void showNoUnRelativeDevicesView(int flag);                     //显示没有未关联设备文案

        void setOnUnRelItemClickListener(UnRelationDoorBellAdapter.OnRelativeClickListener listener);

        void setOnRelaItemClickListener(RelationDoorBellAdapter.OnUnRelaItemClickListener listener);

        void notifyUnRelativeRecycle(SuperViewHolder holder, int viewType, int layoutPosition, BellInfoBean item, int flag);

        void notifyRelativeRecycle(SuperViewHolder holder, int viewType, int layoutPosition, BellInfoBean item, int flag);

        void showProgress();

        void hideProgress();
    }

    interface Presenter extends BasePresenter {
        void loadDoorBellData(String url);

        void loadUnRelaiveDoorBellData(String url);
    }

}
