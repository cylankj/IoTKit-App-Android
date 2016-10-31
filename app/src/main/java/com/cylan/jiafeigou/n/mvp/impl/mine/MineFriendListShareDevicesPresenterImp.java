package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendListShareDevicesToContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineShareDeviceBean;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendListShareDevicesPresenterImp extends AbstractPresenter<MineFriendListShareDevicesToContract.View> implements MineFriendListShareDevicesToContract.Presenter {

    public MineFriendListShareDevicesPresenterImp(MineFriendListShareDevicesToContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public ArrayList<MineShareDeviceBean> getDeviceData() {
        ArrayList<MineShareDeviceBean> list = new ArrayList<>();
        MineShareDeviceBean mineShareDeviceBean = new MineShareDeviceBean();
        mineShareDeviceBean.setCheck(true);
        mineShareDeviceBean.setDeviceName("智能摄像头");
        mineShareDeviceBean.setIconUrl("");
        mineShareDeviceBean.setShareNumber(2);
        list.add(mineShareDeviceBean);
        return list;
    }

    @Override
    public boolean checkListEmpty(ArrayList<MineShareDeviceBean> list) {
        return list.size() == 0 ? true : false;
    }
}
