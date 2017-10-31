package com.cylan.jiafeigou.n.view.record;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yzd on 16-12-16.
 */

public class DelayRecordGuideFragment extends BaseFragment {

    @BindView(R.id.header_delay_record_container)
    ViewGroup mHeaderContainer;
    @Inject
    JFGSourceManager sourceManager;

    public static DelayRecordGuideFragment newInstance(String uuid) {
        DelayRecordGuideFragment fragment = new DelayRecordGuideFragment();
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(bundle);
        return fragment;
    }

    private WeakReference<BaseDialog> mEnableDeviceDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delay_record_guide, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewUtils.setViewMarginStatusBar(mHeaderContainer);
    }

    @Override
    public void onStop() {
        super.onStop();
        ViewUtils.clearViewMarginStatusBar(mHeaderContainer);
    }

    @OnClick(R.id.header_delay_record_back)
    public void back() {
        getActivity().onBackPressed();
    }

    @OnClick(R.id.fragment_delay_record_start_now)
    public void startNow() {
        if (isDeviceSleeping()) {
            //TODO:关闭设备待机模式,需要跳转另一个activity,以后有时间再写
            initDeviceEnableDialog();
            mEnableDeviceDialog.get().show(getChildFragmentManager(), BaseDialog.class.getName());
        } else {
            // TODO: 2017/10/26  
//            onViewActionToActivity(JFGView.VIEW_ACTION_OK, DelayRecordContract.View.VIEW_HANDLER_TO_MAIN_VIEW, uuid);
        }
    }

    private boolean isDeviceSleeping() {
        Device device = sourceManager.getDevice(uuid);
        DpMsgDefine.DPStandby isStandBY = device.$(DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, new DpMsgDefine.DPStandby());
        return isStandBY.standby;
    }

    private void initDeviceEnableDialog() {
        if (mEnableDeviceDialog == null || mEnableDeviceDialog.get() == null) {
            mEnableDeviceDialog = new WeakReference<>(new BaseDialog());
            View view = View.inflate(getContext(), R.layout.dialog_enable_device, null);
            mEnableDeviceDialog.get().setContentView(view);
        }
    }
}
