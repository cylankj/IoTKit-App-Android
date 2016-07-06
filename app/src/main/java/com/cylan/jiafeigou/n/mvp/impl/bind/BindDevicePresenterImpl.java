package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.jiafeigou.n.mvp.contract.bind.BindDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * Created by cylan-hunt on 16-7-6.
 */
public class BindDevicePresenterImpl extends AbstractPresenter<BindDeviceContract.View> implements BindDeviceContract.Presenter {


    public BindDevicePresenterImpl(BindDeviceContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void scanDevices() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
