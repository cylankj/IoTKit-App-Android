package com.cylan.jiafeigou.n.mvp.contract.mine;

import android.view.View;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.view.adapter.MineShareDeviceAdapter;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public interface MineShareDeviceContract {

    interface View extends BaseView<Presenter> {
        void showShareDialog();
        void initRecycleView(MineShareDeviceAdapter adapter);
        void jump2ShareDeviceMangerFragment(android.view.View itemView, int viewType, int position);
    }

    interface Presenter extends BasePresenter {
        void initData();
        DeviceBean getBean(int position);
    }

}
