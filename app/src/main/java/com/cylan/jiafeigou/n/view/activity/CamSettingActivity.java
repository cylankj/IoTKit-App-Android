package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
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
    private Device device;
    private WeakReference<DeviceInfoDetailFragment> informationWeakReference;
    private WeakReference<VideoAutoRecordFragment> videoAutoRecordFragmentWeakReference;

    private DpMsgDefine.DPStandby dpStandby;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_setting);
        ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        device = DataSourceManager.getInstance().getJFGDevice(this.uuid);
        if (TextUtils.isEmpty(uuid)) {
            AppLogger.e("uuid is null");
            finishExt();
            return;
        }
        basePresenter = new CamSettingPresenterImpl(this, uuid);
        if (getIntent().getBooleanExtra(JConstant.KEY_JUMP_TO_CAM_DETAIL, false)) {
            jumpDetail(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initBackListener();
    }

    private void initSightBtn() {
        if (JFGRules.isShareDevice(uuid)) {
            sbtnSettingSight.setVisibility(View.GONE);
            return;
        }
        if (device != null && JFGRules.isPanoramicCam(device.pid)) {
            sbtnSettingSight.setVisibility(View.VISIBLE);
            int defaultValue = PreferencesUtils.getInt(JConstant.KEY_CAM_SIGHT_HORIZONTAL + uuid, 0);
            sbtnSettingSight.setTvSubTitle(getString(defaultValue == 0 ? R.string.Tap1_Camera_Front : R.string.Tap1_Camera_Overlook));
        }
    }

    private void initBackListener() {
        customToolbar.post(() -> {
            customToolbar.setBackAction((View v) -> {
                finishExt();
            });
        });
    }


    @Override
    public void onBackPressed() {
        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;
        finishExt();
    }


    private void jumpDetail(boolean animation) {
        initInfoDetailFragment();
        DeviceInfoDetailFragment fragment = informationWeakReference.get();
        fragment.setCallBack((Object t) -> {
            deviceUpdate(DataSourceManager.getInstance().getRawJFGDevice(uuid));
        });
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(bundle);
        if (animation) {
            loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment, fragment.getClass().getSimpleName())
//                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
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
                jumpDetail(true);
            }
            break;
            case R.id.tv_setting_unbind: {
                if (NetUtils.getJfgNetType(getContext()) == 0) {
                    ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.SURE_DELETE_1, JFGRules.getDeviceAlias(device)));
                bundle.putBoolean(BaseDialog.KEY_TOUCH_OUT_SIDE_DISMISS, true);
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
                    deviceUpdate(DataSourceManager.getInstance().getRawJFGDevice(uuid));
                });
            }
            break;
            case R.id.sv_setting_safe_protection: {
                Bundle bundle = new Bundle();
                bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
                SafeProtectionFragment fragment = SafeProtectionFragment.newInstance(bundle);
                fragment.setArguments(bundle);
                loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
                fragment.setCallBack((Object t) -> {
                    deviceUpdate(DataSourceManager.getInstance().getRawJFGDevice(uuid));
                });
            }
            break;
            case R.id.sv_setting_device_delay_capture: {
                Intent intent = new Intent(this, DelayRecordActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                intent.putExtra(JConstant.VIEW_CALL_WAY, DelayRecordContract.View.VIEW_LAUNCH_WAY_SETTING);
                startActivity(intent,
                        ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),
                                R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
            }
            break;
            case R.id.sv_setting_device_wifi:
                if (device != null && JFGRules.isFreeCam(device.pid)) {
                    Intent intent = new Intent(this, BindDeviceActivity.class);
                    intent.putExtra(JConstant.KEY_AUTO_SHOW_BIND, JConstant.KEY_AUTO_SHOW_BIND);
                    startActivity(intent);
                } else {
                    DpMsgDefine.DPNet net = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_201_NET);
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
            if (view.getId() == R.id.tv_setting_unbind) {
                continue;//解绑按钮
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


    private void initVideoAutoRecordFragment() {
        //should load
        if (videoAutoRecordFragmentWeakReference == null || videoAutoRecordFragmentWeakReference.get() == null) {
            videoAutoRecordFragmentWeakReference = new WeakReference<>(VideoAutoRecordFragment.newInstance(null));
        }
    }

    @Override
    public void deviceUpdate(JFGDevice device) {
        //////////////////////////分享账号////////////////////////////////////////////
        if (device != null && !TextUtils.isEmpty(device.shareAccount)) {
            //分享账号 隐藏
            final int count = lLayoutSettingItemContainer.getChildCount();
            for (int i = 2; i < count - 1; i++) {
                View v = lLayoutSettingItemContainer.getChildAt(i);
                if (v != null)
                    v.setVisibility(View.GONE);
            }
            return;
        }
        ////////////////////////////////////////////////////////////////////////
        String detailInfo = basePresenter.getDetailsSubTitle(getContext());
        if (!TextUtils.isEmpty(detailInfo) && detailInfo.contains("(")) {
            svSettingDeviceDetail.setTvSubTitle(basePresenter.getDetailsSubTitle(getContext()), android.R.color.holo_red_dark);
        } else
            svSettingDeviceDetail.setTvSubTitle(basePresenter.getDetailsSubTitle(getContext()), R.color.color_8C8C8C);
        ////////////////////////standby////////////////////////////////////////////
        if (device != null && !JFGRules.showStandbyItem(device.pid)) {
            svSettingDeviceStandbyMode.setVisibility(View.GONE);
        } else {
            this.dpStandby = MiscUtils.safeGet_(DataSourceManager.getInstance().getValue(this.uuid, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG), DpMsgDefine.DPStandby.empty());
            ((SwitchButton) svSettingDeviceStandbyMode.findViewById(R.id.btn_item_switch)).setChecked(dpStandby.standby);
            svSettingDeviceStandbyMode.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
//                basePresenter.updateInfoReq(check, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG);
                switchBtn(lLayoutSettingItemContainer, !isChecked);
            });
            if (this.dpStandby.standby) {
                switchBtn(lLayoutSettingItemContainer, false);
            }
            svSettingSafeProtection.setEnabled(dpStandby.standby);
            svSettingSafeProtection.setAlpha(dpStandby.standby ? 0.6f : 1.0f);
            svSettingSafeProtection.setTvSubTitle(dpStandby.standby ? getString(R.string.MAGNETISM_OFF) : basePresenter.getAlarmSubTitle(getContext()));

            svSettingDeviceAutoRecord.setEnabled(dpStandby.standby);
            svSettingDeviceAutoRecord.setAlpha(dpStandby.standby ? 0.6f : 1.0f);
            svSettingDeviceAutoRecord.setTvSubTitle(dpStandby.standby ? "" : basePresenter.getAutoRecordTitle(getContext()));

            /////////////////////////////led/////////////////////////////////////
            if (device != null && JFGRules.showLedIndicator(device.pid)) {
                DpMsgDefine.DPPrimary<Boolean> dpIndicator = DataSourceManager.getInstance().getValue(this.uuid, DpMsgMap.ID_209_LED_INDICATOR);
                boolean state = this.dpStandby != null && !this.dpStandby.standby && MiscUtils.safeGet(dpIndicator, false);
                svSettingDeviceIndicator.setChecked(state);
                svSettingDeviceIndicator.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    DpMsgDefine.DPPrimary<Boolean> check = new DpMsgDefine.DPPrimary<Boolean>();
                    check.value = isChecked;
                    basePresenter.updateInfoReq(check, DpMsgMap.ID_209_LED_INDICATOR);
                });
            } else {
                svSettingDeviceIndicator.setVisibility(View.GONE);
            }
        }

        ////////////////////////////net////////////////////////////////////////
        DpMsgDefine.DPNet net = MiscUtils.safeGet_(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_201_NET), DpMsgDefine.DPNet.empty);
        svSettingDeviceWifi.setTvSubTitle(!TextUtils.isEmpty(net.ssid) ? net.ssid : getString(R.string.OFF_LINE));
        boolean flag = MiscUtils.safeGet(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY), false);
        //是否有sim卡
        svSettingDeviceMobileNetwork.setVisibility(flag ? View.VISIBLE : View.GONE);
        svSettingDeviceMobileNetwork.setChecked(flag);
        if (device != null && JFGRules.is3GCam(device.pid) && JFGRules.isMobileNet(net.net)) {
            DpMsgDefine.DPPrimary<Boolean> state = DataSourceManager.getInstance().getValue(this.uuid, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY);
            boolean s = MiscUtils.safeGet(state, false);
            svSettingDeviceMobileNetwork.setChecked(s);
            svSettingDeviceMobileNetwork.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPPrimary<Boolean> check = new DpMsgDefine.DPPrimary<Boolean>();
                check.value = isChecked;
                basePresenter.updateInfoReq(check, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY);
            });
        } else {
            svSettingDeviceMobileNetwork.setVisibility(View.GONE);
            svSettingDeviceWifi.showDivider(false);
        }
        /////////////////////////////////110v//////////////////////////////////
        if (device != null && (JFGRules.isWifiCam(device.pid) || JFGRules.isPanoramicCam(device.pid))) {
            DpMsgDefine.DPPrimary<Boolean> dpState = DataSourceManager.getInstance().getValue(this.uuid, DpMsgMap.ID_216_DEVICE_VOLTAGE);
            boolean state = MiscUtils.safeGet(dpState, false);
            sbtnSetting110v.setChecked(state);
            sbtnSetting110v.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPPrimary<Boolean> check = new DpMsgDefine.DPPrimary<Boolean>();
                check.value = isChecked;
                basePresenter.updateInfoReq(check, DpMsgMap.ID_216_DEVICE_VOLTAGE);
            });
        } else sbtnSetting110v.setVisibility(View.GONE);

        /////////////////////////旋转/////////////////////////////////////////
        if (device != null && JFGRules.isPanoramicCam(device.pid)) {
            svSettingDeviceRotate.setVisibility(View.GONE);
        } else {
            DpMsgDefine.DPPrimary<Integer> state = DataSourceManager.getInstance().getValue(this.uuid, DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE);
            svSettingDeviceRotate.setChecked(MiscUtils.safeGet(state, 0) == 1);
            svSettingDeviceRotate.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPPrimary<Integer> check = new DpMsgDefine.DPPrimary<>();
                check.value = isChecked ? 1 : 0;
                basePresenter.updateInfoReq(check, DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE);
            });
        }
        /////////////////////////////////////////////////////////////////////
        if (device != null && JFGRules.showDelayRecordBtn(device.pid)) {
            svSettingDeviceDelayCapture.setEnabled(true);
        } else {
            svSettingDeviceDelayCapture.setVisibility(View.GONE);
        }

    }

    @Override
    public void unbindDeviceRsp(int state) {
        if (state == JError.ErrorOK) {
            LoadingDialog.dismissLoading(getSupportFragmentManager());
            Intent intent = new Intent(this, NewHomeActivity.class);
            intent.putExtra("NewHomeActivity_intent", getString(R.string.DELETED_SUC));
            startActivity(intent);
        } else if (state == -1) {
            ToastUtil.showToast(getString(R.string.Tips_DeleteFail));
            LoadingDialog.dismissLoading(getSupportFragmentManager());
        }
    }

    @Override
    public void onNetworkChanged(boolean connected) {
        svSettingDeviceMobileNetwork.setEnabled(connected);
        svSettingDeviceDelayCapture.setEnabled(connected);
        svSettingDeviceStandbyMode.setEnabled(connected);
        if (!connected) {
            svSettingDeviceMobileNetwork.setAlpha(0.6f);
            svSettingDeviceDelayCapture.setAlpha(0.6f);
            svSettingDeviceStandbyMode.setAlpha(0.6f);
        } else {
            svSettingDeviceMobileNetwork.setAlpha(1.0f);
            svSettingDeviceDelayCapture.setAlpha(1.0f);
            svSettingDeviceStandbyMode.setAlpha(1.0f);
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
