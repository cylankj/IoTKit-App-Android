package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.CamInfoBean;
import com.cylan.jiafeigou.n.view.cam.DeviceStandbyFragment;
import com.cylan.jiafeigou.n.view.cam.FragmentFacilityInformation;
import com.cylan.jiafeigou.n.view.cam.SafeProtectionFragment;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.SettingItemView1;
import com.kyleduo.switchbutton.SwitchButton;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CamSettingActivity extends BaseFullScreenFragmentActivity
        implements CamSettingContract.View {

    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.sv_setting_device_detail)
    SettingItemView0 svSettingDeviceDetail;
    @BindView(R.id.sv_setting_device_wifi)
    SettingItemView0 svSettingDeviceWifi;
    @BindView(R.id.sv_setting_device_mobile_network)
    SettingItemView1 svSettingDeviceMobileNetwork;
    @BindView(R.id.sv_setting_safe_protection)
    SettingItemView0 svSettingSafeProtection;
    @BindView(R.id.sv_setting_device_auto_record)
    SettingItemView0 svSettingDeviceAutoRecord;
    @BindView(R.id.sv_setting_device_delay_capture)
    SettingItemView0 svSettingDeviceDelayCapture;
    @BindView(R.id.sv_setting_device_standby_mode)
    SettingItemView0 svSettingDeviceStandbyMode;
    @BindView(R.id.sv_setting_device_indicator)
    SettingItemView1 svSettingDeviceIndicator;
    @BindView(R.id.sv_setting_device_rotatable)
    SettingItemView1 svSettingDeviceRotate;
    @BindView(R.id.sbtn_setting_item_switch_110v)
    SwitchButton tbSettingItemSwitch110v;
    @BindView(R.id.tv_setting_unbind)
    TextView tvSettingUnbind;

    private CamSettingContract.Presenter presenter;

    private WeakReference<FragmentFacilityInformation> informationWeakReference;
    private WeakReference<DeviceStandbyFragment> deviceStandbyFragmentWeakReference;
    private WeakReference<SafeProtectionFragment> safeProtectionFragmentWeakReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_setting);
        ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        presenter = new CamSettingPresenterImpl(this);
        presenter.fetchCamInfo();
        initTopBar();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initTopBar() {
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }

    @Override
    public void onBackPressed() {
        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            overridePendingTransition(R.anim.slide_in_left_without_interpolator, R.anim.slide_out_right_without_interpolator);
        }
    }

    @OnClick(R.id.imgV_top_bar_center)
    public void onBackClick() {
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            overridePendingTransition(R.anim.slide_in_left_without_interpolator, R.anim.slide_out_right_without_interpolator);
        }
    }

    @OnClick({R.id.sv_setting_device_detail,
            R.id.sv_setting_device_indicator,
            R.id.sv_setting_device_rotatable,
            R.id.sbtn_setting_item_switch_110v,
            R.id.tv_setting_unbind,
            R.id.sv_setting_device_standby_mode,
            R.id.sv_setting_safe_protection,
            R.id.sv_setting_device_mobile_network})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sv_setting_device_detail:
                initFacilityFragment();
                loadFragment(android.R.id.content, informationWeakReference.get());
                break;
            case R.id.sv_setting_device_indicator:
                if (presenter != null) {
                    CamInfoBean camInfoBean = presenter.getCamInfoBean();
                    camInfoBean.isDeviceIndicator = !camInfoBean.isDeviceIndicator;
                    presenter.saveCamInfoBean(camInfoBean);
                }
                break;
            case R.id.sv_setting_device_rotatable:
                if (presenter != null) {
                    CamInfoBean camInfoBean = presenter.getCamInfoBean();
                    camInfoBean.isRotatable = !camInfoBean.isRotatable;
                    presenter.saveCamInfoBean(camInfoBean);
                }
                break;
            case R.id.sbtn_setting_item_switch_110v:
                if (presenter != null) {
                    CamInfoBean camInfoBean = presenter.getCamInfoBean();
                    camInfoBean.isSupport110V = !camInfoBean.isSupport110V;
                    presenter.saveCamInfoBean(camInfoBean);
                }
                break;
            case R.id.sv_setting_device_mobile_network:
                if (presenter != null) {
                    CamInfoBean camInfoBean = presenter.getCamInfoBean();
                    camInfoBean.isMobileNet = !camInfoBean.isMobileNet;
                    presenter.saveCamInfoBean(camInfoBean);
                }
                break;
            case R.id.tv_setting_unbind:
                break;
            case R.id.sv_setting_safe_protection:
                initSafeProtectionFragment();
                loadFragment(android.R.id.content, safeProtectionFragmentWeakReference.get());
                break;
            case R.id.sv_setting_device_standby_mode:
                initStandbyInfoFragment();
                loadFragment(android.R.id.content, deviceStandbyFragmentWeakReference.get());
                break;
        }
    }

    /**
     * 用来加载fragment的方法。
     */
    private void loadFragment(int id, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                //如果需要动画，可以把动画添加进来
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(id, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    private void initFacilityFragment() {
        //should load
        if (informationWeakReference == null || informationWeakReference.get() == null) {
            informationWeakReference = new WeakReference<>(FragmentFacilityInformation.newInstance(new Bundle()));
        }
    }

    private void initStandbyInfoFragment() {
        //should load
        if (deviceStandbyFragmentWeakReference == null || deviceStandbyFragmentWeakReference.get() == null) {
            deviceStandbyFragmentWeakReference = new WeakReference<>(DeviceStandbyFragment.newInstance(new Bundle()));
        }
    }

    private void initSafeProtectionFragment() {
        //should load
        if (safeProtectionFragmentWeakReference == null || safeProtectionFragmentWeakReference.get() == null) {
            safeProtectionFragmentWeakReference = new WeakReference<>(SafeProtectionFragment.newInstance(new Bundle()));
        }
    }

    @Override
    public void onCamInfoRsp(CamInfoBean camInfoBean) {
        svSettingDeviceWifi.setTvSubTitle(camInfoBean.ssid);
        svSettingDeviceMobileNetwork.setSwitchButtonState(camInfoBean.isMobileNet);
        svSettingSafeProtection.setTvSubTitle(camInfoBean.safeSummary);
        svSettingDeviceStandbyMode.setTvSubTitle(camInfoBean.isStandby ? "开启" : "关闭");
        svSettingDeviceIndicator.setSwitchButtonState(camInfoBean.isDeviceIndicator);
        svSettingDeviceRotate.setSwitchButtonState(camInfoBean.isRotatable);
        tbSettingItemSwitch110v.setChecked(camInfoBean.isSupport110V);
    }

    @Override
    public void setPresenter(CamSettingContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
