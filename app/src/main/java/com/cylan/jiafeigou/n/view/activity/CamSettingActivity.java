package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamSettingPresenterImpl;
import com.cylan.jiafeigou.n.view.cam.DeviceInfoDetailFragment;
import com.cylan.jiafeigou.n.view.cam.SafeProtectionFragment;
import com.cylan.jiafeigou.n.view.cam.VideoAutoRecordFragment;
import com.cylan.jiafeigou.n.view.record.DelayRecordActivity;
import com.cylan.jiafeigou.n.view.setting.WifiListFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
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
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.sbtn_setting_sight)
    SettingItemView0 sbtnSettingSight;
    private String uuid;
    private JFGDevice device;
    private WeakReference<DeviceInfoDetailFragment> informationWeakReference;
    private WeakReference<SafeProtectionFragment> safeProtectionFragmentWeakReference;
    private WeakReference<VideoAutoRecordFragment> videoAutoRecordFragmentWeakReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_setting);
        ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        device = GlobalDataProxy.getInstance().fetch(this.uuid);
        if (TextUtils.isEmpty(uuid)) {
            AppLogger.e("uuid is null");
            finish();
            return;
        }
        basePresenter = new CamSettingPresenterImpl(this, uuid);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSightBtn();
        initStandbyBtn();
        init110VVoltageBtn();
        initLedIndicatorBtn();
        initMobileNetBtn();
        initRotateBtn();
        initDelayRecordBtn();
        initBackListener();
    }

    private void initSightBtn() {
        if (device != null && JFGRules.isPanoramicCam(device.pid)) {
            sbtnSettingSight.setVisibility(View.VISIBLE);
            int defaultValue = PreferencesUtils.getInt(JConstant.KEY_CAM_SIGHT_HORIZONTAL, 0);
            sbtnSettingSight.setTvSubTitle(getString(defaultValue == 0 ? R.string.Tap1_Camera_Front : R.string.Tap1_Camera_Overlook));
        }
    }

    private void initBackListener() {
        customToolbar.post(() -> {
            customToolbar.setBackAction((View v) -> {
                finish();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    overridePendingTransition(R.anim.slide_in_left_without_interpolator, R.anim.slide_out_right_without_interpolator);
                }
            });
        });
    }

    private void initDelayRecordBtn() {
        if (device != null && JFGRules.showDelayRecordBtn(device.pid)) {
            svSettingDeviceDelayCapture.setEnabled(true);
        } else {
            svSettingDeviceDelayCapture.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if (device != null && JFGRules.isFreeCam(device.pid)) {
            svSettingDeviceStandbyMode.setVisibility(View.GONE);
        }
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
        BaseValue baseValue = GlobalDataProxy.getInstance().fetchLocal(this.uuid, DpMsgMap.ID_201_NET);
        DpMsgDefine.DPNet net = baseValue == null ? null : baseValue.getValue();
        if (device != null && JFGRules.is3GCam(device.pid) && net != null && JFGRules.isMobileNet(net.net)) {
            boolean state = GlobalDataProxy.getInstance().getValue(this.uuid, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY, false);
            ((SwitchButton) svSettingDeviceMobileNetwork.findViewById(R.id.btn_item_switch)).setChecked(state);
            ((SwitchButton) svSettingDeviceMobileNetwork.findViewById(R.id.btn_item_switch))
                    .setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                        basePresenter.updateInfoReq(isChecked, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY);
                    });
        } else svSettingDeviceMobileNetwork.setVisibility(View.GONE);
    }

    private void init110VVoltageBtn() {
        if (device != null && (JFGRules.isWifiCam(device.pid) || JFGRules.isPanoramicCam(device.pid))) {
            boolean state = GlobalDataProxy.getInstance().getValue(this.uuid, DpMsgMap.ID_216_DEVICE_VOLTAGE, false);
            ((SwitchButton) sbtnSetting110v.findViewById(R.id.btn_item_switch)).setChecked(state);
            ((SwitchButton) sbtnSetting110v.findViewById(R.id.btn_item_switch))
                    .setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                        basePresenter.updateInfoReq(isChecked, DpMsgMap.ID_216_DEVICE_VOLTAGE);
                    });
        } else sbtnSetting110v.setVisibility(View.GONE);
    }

    private void initLedIndicatorBtn() {
        if (device != null && JFGRules.showLedIndicator(device.pid)) {
            boolean standby = GlobalDataProxy.getInstance().getValue(this.uuid, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, false);
            boolean state = !standby && GlobalDataProxy.getInstance().getValue(this.uuid, DpMsgMap.ID_209_LED_INDICATOR, true);
            ((SwitchButton) svSettingDeviceIndicator.findViewById(R.id.btn_item_switch)).setChecked(state);
            ((SwitchButton) svSettingDeviceIndicator.findViewById(R.id.btn_item_switch))
                    .setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                        basePresenter.updateInfoReq(isChecked, DpMsgMap.ID_209_LED_INDICATOR);
                    });
        } else {
            svSettingDeviceIndicator.setVisibility(View.GONE);
        }
    }

    private void initRotateBtn() {
        if (device != null && JFGRules.isPanoramicCam(device.pid)) {
            svSettingDeviceIndicator.setVisibility(View.GONE);
        } else {
            int state = GlobalDataProxy.getInstance().getValue(this.uuid, DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE, 0);
            ((SwitchButton) svSettingDeviceIndicator.findViewById(R.id.btn_item_switch)).setChecked(state != 0);
            ((SwitchButton) svSettingDeviceRotate.findViewById(R.id.btn_item_switch))
                    .setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                        basePresenter.updateInfoReq(isChecked ? 1 : 0, DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE);
                    });
        }

    }

    @OnClick({R.id.sv_setting_device_detail,
            R.id.sv_setting_device_auto_record,
            R.id.sv_setting_safe_protection,
            R.id.tv_setting_unbind,
            R.id.sv_setting_device_delay_capture,
            R.id.sv_setting_device_wifi,
            R.id.sbtn_setting_sight
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
                bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.DELETE_SURE));
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
                Intent intent = new Intent(this, DelayRecordActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                intent.putExtra(JConstant.VIEW_CALL_WAY, DelayRecordContract.View.VIEW_LAUNCH_WAY_SETTING);
//                startActivity(intent);
                startActivity(intent,

                        ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),
                                R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
            }
            case R.id.sv_setting_device_wifi:
                if (device != null && JFGRules.isFreeCam(device.pid)) {
                    Intent intent = new Intent(this, BindDeviceActivity.class);
                    intent.putExtra(JConstant.KEY_AUTO_SHOW_BIND, JConstant.KEY_AUTO_SHOW_BIND);
                    startActivity(intent);
                } else {
                    DpMsgDefine.DPNet net = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_201_NET, DpMsgDefine.DPNet.empty);
                    if (!JFGRules.isDeviceOnline(net)) {
                        //设备离线
                        Intent intent = new Intent(this, BindDeviceActivity.class);
                        intent.putExtra(JConstant.KEY_AUTO_SHOW_BIND, JConstant.KEY_AUTO_SHOW_BIND);
                        startActivity(intent);
                    } else {
                        //设备在线
                        String localSSid = NetUtils.getNetName(ContextUtils.getContext());
                        String remoteSSid = net.ssid;
                        if (!TextUtils.equals(localSSid, remoteSSid)) {
                            new AlertDialog.Builder(this)
                                    .setMessage(getString(R.string.setwifi_check, remoteSSid))
                                    .setNegativeButton(getString(R.string.CANCEL), null)
                                    .setPositiveButton(getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                    })
                                    .show();
                        } else {
                            //显示列表
//                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            Bundle bundle = new Bundle();
                            bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
                            WifiListFragment fragment = WifiListFragment.getInstance(bundle);
                            loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
                        }
                    }
                }
                break;
            case R.id.sbtn_setting_sight:
                Intent intent = new Intent(this, SightSettingActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                startActivity(intent);
                break;
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
            if (view.getId() == R.id.sv_setting_device_wifi) {
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
            if (view.getId() == R.id.sbtn_setting_sight) {
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
            DpMsgDefine.DPNet net = MiscUtils.cast(value.getValue(), null);
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
