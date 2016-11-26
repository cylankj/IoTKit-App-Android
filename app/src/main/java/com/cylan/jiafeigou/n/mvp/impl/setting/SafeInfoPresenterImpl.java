package com.cylan.jiafeigou.n.mvp.impl.setting;

import com.cylan.jiafeigou.n.mvp.contract.setting.SafeInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class SafeInfoPresenterImpl extends AbstractPresenter<SafeInfoContract.View>
        implements SafeInfoContract.Presenter {

    private BeanCamInfo beanCamInfo;

    public SafeInfoPresenterImpl(SafeInfoContract.View view, BeanCamInfo beanCamInfo) {
        super(view);
        this.beanCamInfo = beanCamInfo;
        view.setPresenter(this);
    }

    @Override
    public void updateInfo(BeanCamInfo beanCamInfo, int... id) {
        this.beanCamInfo = beanCamInfo;

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
