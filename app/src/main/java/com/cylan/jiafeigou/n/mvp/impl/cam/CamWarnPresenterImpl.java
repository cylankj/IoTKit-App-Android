package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.jiafeigou.n.mvp.contract.cam.CamWarnContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class CamWarnPresenterImpl extends AbstractPresenter<CamWarnContract.View> implements
        CamWarnContract.Presenter {
    private BeanCamInfo beanCamInfo;

    public CamWarnPresenterImpl(CamWarnContract.View view, BeanCamInfo info) {
        super(view);
        view.setPresenter(this);
        this.beanCamInfo = info;
    }

    @Override
    public void save(BeanCamInfo info) {
        beanCamInfo = info;
    }

    @Override
    public BeanCamInfo getBeanCamInfo() {
        return beanCamInfo;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
