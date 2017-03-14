package com.cylan.jiafeigou.n.view.panorama;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.n.view.cam.DeviceInfoDetailFragment;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;
import static com.cylan.jiafeigou.utils.ActivityUtils.loadFragment;

/**
 * Created by yanzhendong on 2017/3/11.
 */

public class PanoramaSettingFragment extends BaseFragment<PanoramaSettingContact.Presenter> {
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout toolbarContainer;

    public static PanoramaSettingFragment newInstance(String uuid) {
        PanoramaSettingFragment fragment = new PanoramaSettingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected PanoramaSettingContact.Presenter onCreatePresenter() {
        return new PanoramaSettingPresenter();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewUtils.setViewPaddingStatusBar(toolbarContainer);
    }

    @Override
    public void onStop() {
        super.onStop();
        ViewUtils.clearViewPaddingStatusBar(toolbarContainer);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_panorama_setting;
    }

    @OnClick(R.id.fragment_panorama_setting_header_back)
    public void exit() {
        getActivity().onBackPressed();
    }

    @OnClick(R.id.sv_setting_device_detail)
    public void showDeviceDetail() {
        DeviceInfoDetailFragment fragment = DeviceInfoDetailFragment.newInstance(null);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DEVICE_ITEM_UUID, mUUID);
        fragment.setArguments(bundle);
        loadFragment(android.R.id.content, getActivity().getSupportFragmentManager(), fragment);
    }
}
