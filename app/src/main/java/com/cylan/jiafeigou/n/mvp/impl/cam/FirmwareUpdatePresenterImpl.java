package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.n.mvp.contract.cam.FirmwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class FirmwareUpdatePresenterImpl extends AbstractPresenter<FirmwareUpdateContract.View> implements FirmwareUpdateContract.Presenter,
        SimulatePercent.OnAction {


    public FirmwareUpdatePresenterImpl(FirmwareUpdateContract.View view) {
        super(view);
    }

    @Override
    public void actionDone() {

    }

    @Override
    public void actionPercent(int percent) {

    }
}
