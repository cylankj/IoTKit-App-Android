package com.cylan.jiafeigou.n.mvp.impl.record;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/*
 *  @项目名：  JFGAndroid 
 *  @包名：    com.cylan.jiafeigou.n.mvp.impl.record
 *  @文件名:   DelayRecordPresenterImpl
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/4 22:02
 *  @描述：    TODO
 */
public class DelayRecordPresenterImpl extends BasePresenter<DelayRecordContract.View> implements DelayRecordContract.Presenter {

    private List<String> mUsableDevices;

    @Override
    public void onSetContentView() {
        setupLaunchView();
    }

    /**
     * 刚进来时主动刷新一次设备列表
     */
    private Subscription getFetchDeviceListSub() {
        ArrayList<JFGDevice> devices = GlobalDataProxy.getInstance().fetchAll();
        mUsableDevices = new ArrayList<>();
        if (devices != null) {
            for (JFGDevice device : devices) {
                if (1017 == device.pid) {//3G狗设备,只有3G狗设备才能开启延时摄影
                    mUsableDevices.add(device.uuid);
                }
            }
        }
        if (mUsableDevices != null && mUsableDevices.size() > 1) {//有多于一个可用的设备,则显示设备选择页面
            mView.onShowRecordDeviceView(mUsableDevices);
        } else if (mUsableDevices != null && mUsableDevices.size() == 1) {//只有一个可用的设备,则直接进入延时摄影主页
            mView.onShowRecordMainView(mUsableDevices.get(0));
        } else {
            mView.onShowNoDeviceView();
        }
        return RxBus.getCacheInstance().toObservableSticky(RxUiEvent.BulkUUidListRsp.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp != null && rsp.allList != null) {
                        for (String s : rsp.allList) {
                            if (GlobalDataProxy.getInstance().fetch(s).pid == 1017 && !mUsableDevices.contains(s)) {
                                mUsableDevices.add(s);
                            }
                        }
                        if (mUsableDevices != null && mUsableDevices.size() > 1) {//有多于一个可用的设备,则显示设备选择页面
                            mView.onShowRecordDeviceView(mUsableDevices);
                        } else if (mUsableDevices != null && mUsableDevices.size() == 1) {//只有一个可用的设备,则直接进入延时摄影主页
                            mView.onShowRecordMainView(mUsableDevices.get(0));
                        } else {
                            mView.onShowNoDeviceView();
                        }
                    }
                });
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
                if (isFirstEnter(false)) {
                    //第一次进入则显示向导页
                    mView.onShowRecordGuideView(mUUID);
                } else {
                    //不是第一次进入则直接进入预览页
                    mView.onShowRecordMainView(mUUID);
                }
            }
            break;
            case DelayRecordContract.View.VIEW_LAUNCH_WAY_WONDERFUL: {
                //通过每日精彩页进入该页面
                mSubscriptions.add(getFetchDeviceListSub());
            }
            break;
        }
    }

    /**
     * 是每台设备都进入向导页还是只是第一次进入向导页？暂且第一次
     */
    private boolean isFirstEnter(boolean save) {
        boolean showGuide = PreferencesUtils.getBoolean(JConstant.KEY_DELAY_RECORD_GUIDE, true);
        if (save) PreferencesUtils.putBoolean(JConstant.KEY_DELAY_RECORD_GUIDE, false);
        return showGuide;
    }


    @Override
    public void onViewAction(int action, String handle, Object extra) {
        switch (handle) {
            case DelayRecordContract.View.VIEW_HANDLER_GUIDE_START_NOW:
                //这里进入预览页
                mView.onShowRecordMainView(mUUID);
                break;
            case DelayRecordContract.View.VIEW_HANDLER_GUIDE_ENABLE_DEVICE:
                //这里需要跳转到设备设置activity,比较复杂,以后再慢慢写
                mView.onShowDeviceSettingView(mUUID);
                break;
        }
    }
}
