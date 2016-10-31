package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.MineShareDeviceBean;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineFriendListShareDevicesToContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {
        ArrayList<MineShareDeviceBean> getDeviceData();

        boolean checkListEmpty(ArrayList<MineShareDeviceBean> list);
    }

}
