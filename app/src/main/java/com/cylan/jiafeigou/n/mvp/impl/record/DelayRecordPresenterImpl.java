package com.cylan.jiafeigou.n.mvp.impl.record;

import android.text.TextUtils;

import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.List;

/*
 *  @项目名：  JFGAndroid 
 *  @包名：    com.cylan.jiafeigou.n.mvp.impl.record
 *  @文件名:   DelayRecordPresenterImpl
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/4 22:02
 *  @描述：    TODO
 */
public class DelayRecordPresenterImpl extends BasePresenter<DelayRecordContract.View> implements DelayRecordContract.Presenter {

    private String mLaunchType;

    @Override
    public void onSetContentView() {
        setupLaunchView();
    }

    /**
     * 在获取到设备列表后根据launchViewType设置显示页
     */
    private void setupLaunchView() {
        mLaunchType = mView.onResolveViewLaunchType();
        if (TextUtils.isEmpty(mLaunchType)) {
            //在这里设置默认的view
            return;
        }

        switch (mLaunchType) {
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
                List<String> list = sourceManager.getDeviceUuidByPid(JConstant.OS_CAMERA_ANDROID);
                if (list != null && list.size() == 1) {
                    mView.onShowRecordMainView(list.get(0));
                } else {
                    mView.onShowRecordDeviceView(list);
                }
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
            case DelayRecordContract.View.VIEW_HANDLER_TO_MAIN_VIEW:
                //这里进入预览页
                mView.onShowRecordMainView((String) extra);
                break;
            case DelayRecordContract.View.VIEW_HANDLER_ENABLE_DEVICE:
                //这里需要跳转到设备设置activity,比较复杂,以后再慢慢写
                mView.onShowDeviceSettingView((String) extra);
                break;
        }
    }
}
