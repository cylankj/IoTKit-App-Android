package com.cylan.jiafeigou.n.mvp.impl.cloud;

import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLiveSettingPresenterImp extends AbstractPresenter<CloudLiveSettingContract.View> implements CloudLiveSettingContract.Presenter{

    public CloudLiveSettingPresenterImp(CloudLiveSettingContract.View view) {
        super(view);
    }

    @Override
    public void start() {
        if(getView() != null){
            getView().initSomeViewVisible(isHasBeenShareUser());
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isHasBeenShareUser() {
        //TODO 查询用户的设备是否有绑定改云相框
        return false;
    }
}
