package com.cylan.jiafeigou.n.mvp.impl.cloud;

import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudCorrelationDoorBellContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/29
 * 描述：
 */
public class CloudCorrelationDoorBellPresenterImp extends AbstractPresenter<CloudCorrelationDoorBellContract.View> implements CloudCorrelationDoorBellContract.Presenter{

    public CloudCorrelationDoorBellPresenterImp(CloudCorrelationDoorBellContract.View view) {
        super(view);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
