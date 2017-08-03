package com.cylan.jiafeigou.n.view.panorama;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.databinding.FragmentDeviceConnectionDescriptionBinding;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.bind.PanoramaExplainFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.BindUtils;

/**
 * Created by yanzhendong on 2017/6/6.
 */

public class DeviceConnectionDescriptionFragment extends BaseFragment {

    private FragmentDeviceConnectionDescriptionBinding descriptionBinding;

    @Override
    protected void setFragmentComponent(FragmentComponent fragmentComponent) {
        //do nothing
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        descriptionBinding = FragmentDeviceConnectionDescriptionBinding.inflate(inflater);
        descriptionBinding.setupWifi.setOnClickListener(this::setUpWiFi);
        descriptionBinding.setupAp.setOnClickListener(this::setUpAP);
        descriptionBinding.customToolbar.setRightAction(this::showExplain);
        descriptionBinding.customToolbar.setBackAction(v -> getActivity().onBackPressed());
        return descriptionBinding.getRoot();
    }

    private void showExplain(View view) {
        PanoramaExplainFragment fragment = PanoramaExplainFragment.newInstance(null);
        ActivityUtils.addFragmentSlideInFromRight(getFragmentManager(), fragment, R.id.root);
    }

    private void setUpAP(View view) {
        AppLogger.d("将配置户外模式");
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        Intent intent = BindUtils.getIntentByPid(device.pid, getContext());
        intent.putExtra("PanoramaConfigure", "OutDoor");
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        startActivity(intent);
    }

    private void setUpWiFi(View view) {
        AppLogger.d("将配置家居模式");
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        Intent intent = BindUtils.getIntentByPid(device.pid, getContext());
        intent.putExtra("PanoramaConfigure", "Family");
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        startActivity(intent);
    }

    public static DeviceConnectionDescriptionFragment newInstance(String uuid) {
        DeviceConnectionDescriptionFragment fragment = new DeviceConnectionDescriptionFragment();
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(bundle);
        return fragment;
    }
}
