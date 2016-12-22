package com.cylan.jiafeigou.n.mvp.impl.cloud;

import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveCallInContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/10/19
 * 描述：
 */
public class CloudLiveCallInPresenterImp extends AbstractPresenter<CloudLiveCallInContract.View> implements CloudLiveCallInContract.Presenter {

    public CloudLiveCallInPresenterImp(CloudLiveCallInContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

}
