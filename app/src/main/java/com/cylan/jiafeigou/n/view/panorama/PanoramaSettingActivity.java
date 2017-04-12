package com.cylan.jiafeigou.n.view.panorama;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.view.cam.DeviceInfoDetailFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;
import static com.cylan.jiafeigou.utils.ActivityUtils.loadFragment;

/**
 * Created by yanzhendong on 2017/3/15.
 */

public class PanoramaSettingActivity extends BaseActivity<PanoramaSettingContact.Presenter> implements PanoramaSettingContact.View {
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout toolbarContainer;


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
    protected void setActivityComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_panorama_setting;
    }

    @OnClick(R.id.act_panorama_setting_header_back)
    public void exit() {
        onBackPressed();
    }

    @OnClick(R.id.sv_setting_device_detail)
    public void showDeviceDetail() {
        DeviceInfoDetailFragment fragment = DeviceInfoDetailFragment.newInstance(null);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(bundle);
        loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
    }

    @OnClick(R.id.sv_setting_device_logo)
    public void showDeviceLogoConfigure() {
        AppLogger.d("打开logo 选择页面");
        PanoramaLogoConfigureFragment fragment = PanoramaLogoConfigureFragment.newInstance();
        loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
    }

    @OnClick(R.id.tv_setting_unbind)
    public void unBindDevice() {
        presenter.unBindDevice();
    }

    @Override
    public void unbindDeviceRsp(int resultCode) {
        AppLogger.d("unbindDeviceRsp");
        if (resultCode == JError.ErrorOK) {
            LoadingDialog.dismissLoading(getSupportFragmentManager());
            ToastUtil.showPositiveToast(getString(R.string.DELETED_SUC));
            startActivity(new Intent(this, NewHomeActivity.class));//回到主页
        }
    }
}
