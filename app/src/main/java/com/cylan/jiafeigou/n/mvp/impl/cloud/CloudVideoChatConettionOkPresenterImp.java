package com.cylan.jiafeigou.n.mvp.impl.cloud;

import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudVideoChatConettionOkContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudVideoChatConettionOkPresenterImp extends AbstractPresenter<CloudVideoChatConettionOkContract.View> implements CloudVideoChatConettionOkContract.Presenter {

    public CloudVideoChatConettionOkPresenterImp(CloudVideoChatConettionOkContract.View view) {
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
