package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.view.cam.CamDelayRecordActivity;
import com.cylan.jiafeigou.n.view.cam.DelayRecordGuideFragment;
import com.cylan.jiafeigou.n.view.cam.DeviceInfoDetailFragment;
import com.cylan.jiafeigou.n.view.cam.SafeProtectionFragment;
import com.cylan.jiafeigou.n.view.cam.VideoAutoRecordFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.SettingItemView1;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.kyleduo.switchbutton.SwitchButton;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;
import static com.cylan.jiafeigou.utils.ActivityUtils.loadFragment;

public class CamSettingActivity extends BaseFullScreenFragmentActivity<CamSettingContract.Presenter>
        implements CamSettingContract.View {

    private static final int REQ_DELAY_RECORD = 122;
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
    SettingItemView1 svSettingDeviceStandbyMode;
    @BindView(R.id.sv_setting_device_indicator)
    SettingItemView1 svSettingDeviceIndicator;
    @BindView(R.id.sv_setting_device_rotatable)
    SettingItemView1 svSettingDeviceRotate;
    @BindView(R.id.tv_setting_unbind)
    TextView tvSettingUnbind;
    @BindView(R.id.lLayout_setting_item_container)
    LinearLayout lLayoutSettingItemContainer;
    @BindView(R.id.sbtn_setting_110v)
    SettingItemView0 sbtnSetting110v;
    private String uuid;
    private WeakReference<DeviceInfoDetailFragment> informationWeakReference;
    private WeakReference<SafeProtectionFragment> safeProtectionFragmentWeakReference;
    private WeakReference<VideoAutoRecordFragment> videoAutoRecordFragmentWeakReference;
    private WeakReference<DelayRecordGuideFragment> mGuideFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_setting);
        ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        Bundle bundle = getIntent().getBundleExtra(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        if (bundle == null) {
            AppLogger.e("bundle is null");
            finish();
            return;
        }
        DeviceBean bean = bundle.getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        basePresenter = new CamSettingPresenterImpl(this, bean.uuid);
        this.uuid = bean.uuid;
    }

    @Override
    protected void onStart() {
        super.onStart();
        initTopBar();
        initStandbyBtn();
        init110VVoltageBtn();
        initLedIndicatorBtn();
        initMobileNetBtn();
        initRotateBtn();
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

    /**
     * 待机模式按钮,关联到其他按钮
     */
    private void initStandbyBtn() {
        boolean state = GlobalDataProxy.getInstance().getValue(this.uuid, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, false);
        Log.d("initStandbyBtn", "initStandbyBtn: " + state);
        ((SwitchButton) svSettingDeviceStandbyMode.findViewById(R.id.btn_item_switch)).setChecked(state);
        switchBtn(lLayoutSettingItemContainer, state);
        ((SwitchButton) svSettingDeviceStandbyMode.findViewById(R.id.btn_item_switch))
                .setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    basePresenter.updateInfoReq(isChecked, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG);
                    switchBtn(lLayoutSettingItemContainer, !isChecked);
                });
    }

    private void initMobileNetBtn() {
        boolean state = GlobalDataProxy.getInstance().getValue(this.uuid, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY, false);
        ((SwitchButton) svSettingDeviceMobileNetwork.findViewById(R.id.btn_item_switch)).setChecked(state);
        ((SwitchButton) svSettingDeviceMobileNetwork.findViewById(R.id.btn_item_switch))
                .setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    basePresenter.updateInfoReq(isChecked, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY);
                });
    }

    private void init110VVoltageBtn() {
        boolean state = GlobalDataProxy.getInstance().getValue(this.uuid, DpMsgMap.ID_216_DEVICE_VOLTAGE, false);
        ((SwitchButton) sbtnSetting110v.findViewById(R.id.btn_item_switch)).setChecked(state);
        ((SwitchButton) sbtnSetting110v.findViewById(R.id.btn_item_switch))
                .setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    basePresenter.updateInfoReq(isChecked, DpMsgMap.ID_216_DEVICE_VOLTAGE);
                });
    }

    private void initLedIndicatorBtn() {
        boolean state = GlobalDataProxy.getInstance().getValue(this.uuid, DpMsgMap.ID_209_LED_INDICATOR, false);
        ((SwitchButton) svSettingDeviceIndicator.findViewById(R.id.btn_item_switch)).setChecked(state);
        ((SwitchButton) svSettingDeviceIndicator.findViewById(R.id.btn_item_switch))
                .setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    basePresenter.updateInfoReq(isChecked, DpMsgMap.ID_209_LED_INDICATOR);
                });
    }

    private void initRotateBtn() {
        int state = GlobalDataProxy.getInstance().getValue(this.uuid, DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE, 0);
        ((SwitchButton) svSettingDeviceIndicator.findViewById(R.id.btn_item_switch)).setChecked(state != 0);
        ((SwitchButton) svSettingDeviceRotate.findViewById(R.id.btn_item_switch))
                .setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    basePresenter.updateInfoReq(isChecked ? 1 : 0, DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE);
                });
    }

    @OnClick(R.id.imgV_top_bar_center)
    public void onBackClick() {
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            overridePendingTransition(R.anim.slide_in_left_without_interpolator, R.anim.slide_out_right_without_interpolator);
        }
    }

    @OnClick({R.id.sv_setting_device_detail,
            R.id.sv_setting_device_auto_record,
            R.id.sv_setting_safe_protection,
            R.id.tv_setting_unbind,
            R.id.sv_setting_device_delay_capture
    })
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        switch (view.getId()) {
            case R.id.sv_setting_device_detail: {
                initInfoDetailFragment();
                DeviceInfoDetailFragment fragment = informationWeakReference.get();
                fragment.setCallBack((Object t) -> {
                    onInfoUpdate(null);
                });
                Bundle bundle = new Bundle();
                bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
                fragment.setArguments(bundle);
                loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
            }
            break;
            case R.id.tv_setting_unbind: {
                Bundle bundle = new Bundle();
                bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.DELETE_CID));
                SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.newInstance(bundle);
                simpleDialogFragment.setAction((int id, Object value) -> {
                    basePresenter.unbindDevice();
                    LoadingDialog.showLoading(getSupportFragmentManager(), getString(R.string.DELETEING));
                });
                simpleDialogFragment.show(getSupportFragmentManager(), "simpleDialogFragment");
            }
            break;
            case R.id.sv_setting_device_auto_record: {
                initVideoAutoRecordFragment();
                Bundle bundle = new Bundle();
                bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
                VideoAutoRecordFragment fragment = videoAutoRecordFragmentWeakReference.get();
                fragment.setArguments(bundle);
                loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
                fragment.setCallBack((Object t) -> {
                    onInfoUpdate(null);
                });
            }
            break;
            case R.id.sv_setting_safe_protection: {
                Bundle bundle = new Bundle();
                bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
                initSafeProtectionFragment();
                SafeProtectionFragment fragment = safeProtectionFragmentWeakReference.get();
                fragment.setArguments(bundle);
                loadFragment(android.R.id.content, getSupportFragmentManager(), safeProtectionFragmentWeakReference.get());
                fragment.setCallBack((Object t) -> {
                    onInfoUpdate(null);
                });
            }
            break;
            case R.id.sv_setting_device_delay_capture: {
                if (PreferencesUtils.getBoolean(JConstant.KEY_DELAY_RECORD_GUIDE, true)) {
                    initUserGuideFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(JConstant.KEY_DEVICE_ITEM_BUNDLE, uuid);
                    mGuideFragment.get().setArguments(bundle);
                    ActivityUtils.loadFragment(android.R.id.content, getSupportFragmentManager(), mGuideFragment.get());
                } else {
                    Intent intent = new Intent(this, CamDelayRecordActivity.class);
                    startActivity(intent);
                }
            }
            break;
        }
    }

    private void initUserGuideFragment() {
        if (mGuideFragment == null || mGuideFragment.get() == null) {
            mGuideFragment = new WeakReference<>(DelayRecordGuideFragment.newInstance(null));
        }
    }

    /**
     * 开启待机模式的时候,其余所有选项都不能点击.
     * 递归调用
     *
     * @param viewGroup
     * @param enable
     */
    private void switchBtn(ViewGroup viewGroup, boolean enable) {
        final int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view.getId() == R.id.sv_setting_device_standby_mode) {
                continue;
            }
            if (view.getId() == R.id.sv_setting_device_mobile_network) {
                continue;
            }
            if (view.getId() == R.id.sv_setting_device_detail) {
                continue;
            }
            if (view.getId() == R.id.sv_setting_safe_protection) {
                continue;
            }
            if (view.getId() == R.id.tv_setting_unbind) {
                continue;//解绑按钮
            }
            if (view.getId() == R.id.sv_setting_device_auto_record) {
                continue;//解绑按钮
            }
            if (view instanceof ViewGroup) {
                switchBtn((ViewGroup) view, enable);
            }
            view.setEnabled(enable);
        }
    }


    private void initInfoDetailFragment() {
        //should load
        if (informationWeakReference == null || informationWeakReference.get() == null) {
            informationWeakReference = new WeakReference<>(DeviceInfoDetailFragment.newInstance(null));
        }
    }

    private void initSafeProtectionFragment() {
        //should load
        if (safeProtectionFragmentWeakReference == null || safeProtectionFragmentWeakReference.get() == null) {
            safeProtectionFragmentWeakReference = new WeakReference<>(SafeProtectionFragment.newInstance(new Bundle()));
        }
    }

    private void initVideoAutoRecordFragment() {
        //should load
        if (videoAutoRecordFragmentWeakReference == null || videoAutoRecordFragmentWeakReference.get() == null) {
            videoAutoRecordFragmentWeakReference = new WeakReference<>(VideoAutoRecordFragment.newInstance(null));
        }
    }

    @Override
    public void onInfoUpdate(BaseValue value) {
        int id = value == null ? 0 : (int) value.getId();
        if (id == DpMsgMap.ID_201_NET) {
            DpMsgDefine.MsgNet net = MiscUtils.cast(value.getValue(), null);
            svSettingDeviceWifi.setTvSubTitle(net != null && !TextUtils.isEmpty(net.ssid) ? net.ssid : getString(R.string.OFF_LINE));
        }
        if (id == DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY) {
            boolean flag = MiscUtils.cast(value.getValue(), false);
            svSettingDeviceMobileNetwork.setSwitchButtonState(flag);
        }
        if (id == DpMsgMap.ID_209_LED_INDICATOR) {
            boolean flag = MiscUtils.cast(value.getValue(), false);
            svSettingDeviceIndicator.setSwitchButtonState(flag);
        }
        if (id == DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE) {
            int rotate = MiscUtils.cast(value.getValue(), 0);
            svSettingDeviceRotate.setSwitchButtonState(rotate != 0);
        }
        if (id == DpMsgMap.ID_508_CAMERA_STANDBY_FLAG) {
            boolean flag = MiscUtils.cast(value.getValue(), false);
            svSettingDeviceStandbyMode.setSwitchButtonState(flag);
        }
        svSettingDeviceDetail.setTvSubTitle(basePresenter.getDetailsSubTitle(getContext()));
        svSettingSafeProtection.setTvSubTitle(basePresenter.getAlarmSubTitle(getContext()));
        svSettingDeviceAutoRecord.setTvSubTitle(basePresenter.getAutoRecordTitle(getContext()));
    }

    @Override
    public void isSharedDevice() {
        //分享账号 隐藏
        if (true) return;//doNothing
        final int count = lLayoutSettingItemContainer.getChildCount();
        for (int i = 2; i < count - 1; i++) {
            View v = lLayoutSettingItemContainer.getChildAt(i);
            if (v != null)
                v.setVisibility(View.GONE);
        }
    }

    @Override
    public void unbindDeviceRsp(int state) {
        if (state == JError.ErrorOK) {
            LoadingDialog.dismissLoading(getSupportFragmentManager());
            setResult(RESULT_OK);
            ToastUtil.showPositiveToast(getString(R.string.DOOR_UNBIND));
            finish();
        }
    }

    @Override
    public void setPresenter(CamSettingContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DELAY_RECORD) {

        }
    }
}
