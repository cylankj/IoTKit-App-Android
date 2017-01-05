package com.cylan.jiafeigou.n.mvp.impl.record;

import android.text.TextUtils;

import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/*
 *  @项目名：  JFGAndroid 
 *  @包名：    com.cylan.jiafeigou.n.mvp.impl.record
 *  @文件名:   DelayRecordPresenterImpl
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/4 22:02
 *  @描述：    TODO
 */
public class DelayRecordPresenterImpl extends BasePresenter<DelayRecordContract.View> implements DelayRecordContract.Presenter {

    @Override
    protected void onRegisterSubscription(CompositeSubscription subscriptions) {
        super.onRegisterSubscription(subscriptions);
        subscriptions.add(getFetchDeviceListSub());
    }

    /**
     * 刚进来时主动刷新一次设备列表
     */
    private Subscription getFetchDeviceListSub() {
        return null;
    }


    /**
     * 在获取到设备列表后根据launchViewType设置显示页
     */
    private void setupLaunchView() {
        String launchType = mView.onResolveViewLaunchType();
        if (TextUtils.isEmpty(launchType)) {
            //在这里设置默认的view
            return;
        }

        switch (launchType) {
            case DelayRecordContract.View.VIEW_LAUNCH_WAY_SETTING: {
                //通过设置页进入该页面
                if (isFirstEnter()) {
                    //第一次进入则显示向导页
                } else {
                    //不是第一次进入则直接进入预览页
                }
            }
            break;
            case DelayRecordContract.View.VIEW_LAUNCH_WAY_WONDERFUL: {
                //通过每日精彩页进入该页面

            }
            break;
        }
    }

    private boolean isFirstEnter() {
        return false;
    }


    @Override
    public void onViewAction(int action, String handle, Object extra) {
        switch (handle) {
            case DelayRecordContract.View.VIEW_HANDLER_GUIDE_START_NOW:
                //这里进入预览页
                mView.onShowRecordMainView();
                break;
            case DelayRecordContract.View.VIEW_HANDLER_GUIDE_ENABLE_DEVICE:
                //这里需要跳转到设备设置activity,比较复杂,以后再慢慢写
                break;
        }
    }
}
