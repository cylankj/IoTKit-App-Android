package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.jiafeigou.n.mvp.contract.cam.HardwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class HardwareUpdatePresenterImpl extends AbstractPresenter<HardwareUpdateContract.View> implements HardwareUpdateContract.Presenter {

    private String uuid;

    public HardwareUpdatePresenterImpl(HardwareUpdateContract.View view,String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
    }


}
