package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGCameraDevice;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.SettingTip;
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
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.SettingItemView1;
import com.kyleduo.switchbutton.SwitchButton;

import java.io.IOException;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.dp.DPConstant.SDCARD_STORAGE;
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
    SettingItemView1 svSettingDeviceLedIndicator;
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
    private JFGCameraDevice device;
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
        deviceUpdate(device);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initBackListener();
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
            deviceUpdate(DataSourceManager.getInstance().getJFGDevice(uuid));
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
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.SURE_DELETE_1, JFGRules.getDeviceAlias(device)))
                        .setPositiveButton(getString(R.string.OK), (DialogInterface dialogInterface, int i) -> {
                            basePresenter.unbindDevice();
                            LoadingDialog.showLoading(getSupportFragmentManager(), getString(R.string.DELETEING));
                        })
                        .setNegativeButton(getString(R.string.CANCEL), null)
                        .create().show();
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
                    deviceUpdate(DataSourceManager.getInstance().getJFGDevice(uuid));
                });
                basePresenter.updateSettingTips(basePresenter.getSettingTips().setAutoRecord(0));
            }
            break;
            case R.id.sv_setting_safe_protection: {
                Bundle bundle = new Bundle();
                bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
                SafeProtectionFragment fragment = SafeProtectionFragment.newInstance(bundle);
                fragment.setArguments(bundle);
                loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
                fragment.setCallBack((Object t) -> {
                    deviceUpdate(DataSourceManager.getInstance().getJFGDevice(uuid));
                });
                basePresenter.updateSettingTips(basePresenter.getSettingTips().setSafe(0));
            }
            break;
            case R.id.sv_setting_device_delay_capture: {
                Intent intent = new Intent(this, DelayRecordActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                intent.putExtra(JConstant.VIEW_CALL_WAY, DelayRecordContract.View.VIEW_LAUNCH_WAY_SETTING);
                startActivity(intent,
                        ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),
                                R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
                basePresenter.updateSettingTips(basePresenter.getSettingTips().setAutoRecord(0));
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
                        Intent intent = new Intent(this, BindCamActivity.class);
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
            view.setAlpha(enable ? 1.f : 0.6f);
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
    public void deviceUpdate(JFGCameraDevice device) {
        //////////////////////////分享账号////////////////////////////////////////////
        if (!TextUtils.isEmpty(device.shareAccount)) {
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
        DpMsgDefine.DPSdStatus sdStatus = device.$(SDCARD_STORAGE, new DpMsgDefine.DPSdStatus());
        String detailInfo = basePresenter.getDetailsSubTitle(getContext(), sdStatus.hasSdcard, sdStatus.err);
        if (!TextUtils.isEmpty(detailInfo) && detailInfo.contains("(")) {
            svSettingDeviceDetail.setTvSubTitle(detailInfo, android.R.color.holo_red_dark);
        } else
            svSettingDeviceDetail.setTvSubTitle(detailInfo, R.color.color_8C8C8C);
        ////////////////////////standby////////////////////////////////////////////
        if (!JFGRules.showStandbyItem(device.pid)) {
            svSettingDeviceStandbyMode.setVisibility(View.GONE);
        } else {
            this.dpStandby = MiscUtils.safeGet_(DataSourceManager.getInstance().getValue(this.uuid, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG), DpMsgDefine.DPStandby.empty());
            ((SwitchButton) svSettingDeviceStandbyMode.findViewById(R.id.btn_item_switch)).setChecked(dpStandby.standby);
            svSettingDeviceStandbyMode.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                switchBtn(lLayoutSettingItemContainer, !isChecked);
                this.dpStandby.standby = isChecked;
                this.dpStandby.led = ledPreState();
                this.dpStandby.autoRecord = autoRecordPreState();
                this.dpStandby.warnEnable = warnPreState();
                triggerStandby(isChecked);
                basePresenter.updateInfoReq(dpStandby, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG);
                basePresenter.updateInfoReq(new DpMsgDefine.DPPrimary<>(!isChecked && dpStandby.led), DpMsgMap.ID_209_LED_INDICATOR);
                basePresenter.updateInfoReq(new DpMsgDefine.DPPrimary<>(isChecked ? 0 : dpStandby.autoRecord), DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD);
                basePresenter.updateInfoReq(new DpMsgDefine.DPPrimary<>(isChecked ? 0 : dpStandby.warnEnable), DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
            });
            switchBtn(lLayoutSettingItemContainer, !this.dpStandby.standby);
            triggerStandby(dpStandby.standby);
            /////////////////////////////led/////////////////////////////////////
            if (device != null && JFGRules.showLedIndicator(device.pid)) {
                svSettingDeviceLedIndicator.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    if (this.dpStandby != null && dpStandby.standby) return;//开启待机模式引起的
                    DpMsgDefine.DPPrimary<Boolean> check = new DpMsgDefine.DPPrimary<>();
                    check.value = isChecked;
                    basePresenter.updateInfoReq(check, DpMsgMap.ID_209_LED_INDICATOR);
                    Log.d("check", "led changed: " + isChecked);
                });
            } else {
                svSettingDeviceLedIndicator.setVisibility(View.GONE);
            }
        }

        ////////////////////////////net////////////////////////////////////////
        DpMsgDefine.DPNet net = device.$(JFGCameraDevice.NET, new DpMsgDefine.DPNet());
        svSettingDeviceWifi.setTvSubTitle(!TextUtils.isEmpty(net.ssid) ? net.ssid : getString(R.string.OFF_LINE));
        //是否有sim卡
        int simCard = device.$(DpMsgMap.ID_223_MOBILE_NET, 0);
        svSettingDeviceMobileNetwork.setVisibility(simCard > 1 ? View.VISIBLE : View.GONE);
        svSettingDeviceWifi.showDivider(simCard > 1);
        if (JFGRules.is3GCam(device.pid)) {
            DpMsgDefine.DPPrimary<Boolean> state = DataSourceManager.getInstance().getValue(this.uuid, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY);
            boolean s = MiscUtils.safeGet(state, false);
            svSettingDeviceMobileNetwork.setChecked(s);
            svSettingDeviceMobileNetwork.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPPrimary<Boolean> check = new DpMsgDefine.DPPrimary<>();
                check.value = isChecked;
                basePresenter.updateInfoReq(check, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY);
            });
        }
        /////////////////////////////////110v//////////////////////////////////
        if ((JFGRules.isWifiCam(device.pid) || JFGRules.isPanoramicCam(device.pid))) {
            DpMsgDefine.DPPrimary<Boolean> dpState = DataSourceManager.getInstance().getValue(this.uuid, DpMsgMap.ID_216_DEVICE_VOLTAGE);
            boolean state = MiscUtils.safeGet(dpState, false);
            sbtnSetting110v.setChecked(state);
            sbtnSetting110v.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPPrimary<Boolean> check = new DpMsgDefine.DPPrimary<>();
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

        ////////////////////////显示红点//////////////////////////////////////////////
        SettingTip settingTip = basePresenter.getSettingTips();
        svSettingDeviceAutoRecord.showRedHint(settingTip.autoRecord == 1);
        svSettingSafeProtection.showRedHint(settingTip.safe == 1);
        svSettingDeviceDelayCapture.showRedHint(settingTip.timeLapse == 1);

        if (JFGRules.isShareDevice(uuid)) {
            sbtnSettingSight.setVisibility(View.GONE);
            return;
        }
        if (JFGRules.isPanoramicCam(device.pid)) {
            sbtnSettingSight.setVisibility(View.VISIBLE);
            DpMsgDefine.DpHangMode dpPrimary = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_509_CAMERA_MOUNT_MODE);
            if (dpPrimary == null) dpPrimary = new DpMsgDefine.DpHangMode();
            sbtnSettingSight.setTvSubTitle(getString(dpPrimary.safeGetValue() == 0 ? R.string.Tap1_Camera_Front : R.string.Tap1_Camera_Overlook));
        }
    }

    @Override
    public void deviceUpdate(JFGDPMsg msg) throws IOException {
        switch ((int) msg.id) {
            case 222:
                if (msg.packValue != null) {
                    DpMsgDefine.DPSdcardSummary summary = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
                    if (summary == null) summary = DpMsgDefine.EMPTY.SDCARD_SUMMARY;
                    //sd
                    String statusContent = basePresenter.getDetailsSubTitle(getContext(), summary.hasSdcard, summary.errCode);
                    if (!TextUtils.isEmpty(statusContent) && statusContent.contains("(")) {
                        svSettingDeviceDetail.setTvSubTitle(statusContent, android.R.color.holo_red_dark);
                    } else {
                        svSettingDeviceDetail.setTvSubTitle(statusContent, R.color.color_8c8c8c);
                    }
                }
                break;
            case DpMsgMap.ID_223_MOBILE_NET:
            case DpMsgMap.ID_209_LED_INDICATOR:
                deviceUpdate(DataSourceManager.getInstance().getJFGDevice(uuid));
                //	0 未知, 1 没卡, 2 user's PIN, 3 user's PUK, 4 network PIN, 5 正常
                break;
        }
    }

    private boolean ledPreState() {
        DpMsgDefine.DPPrimary<Boolean> ret = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_209_LED_INDICATOR);
        return MiscUtils.safeGet(ret, false);
    }

    /**
     * 安全防护
     *
     * @return
     */
    private boolean warnPreState() {
        DpMsgDefine.DPPrimary<Boolean> flag = DataSourceManager.getInstance().getValue(uuid, (long) DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
        return MiscUtils.safeGet(flag, false);
    }

    private int autoRecordPreState() {
        return MiscUtils.safeGet(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD), 0);
    }

    private void triggerStandby(boolean triggered) {
        svSettingSafeProtection.setEnabled(!triggered);
        svSettingSafeProtection.setAlpha(triggered ? 0.6f : 1.0f);
        svSettingSafeProtection.setTvSubTitle(triggered ? getString(R.string.MAGNETISM_OFF) : basePresenter.getAlarmSubTitle(getContext()));

        svSettingDeviceAutoRecord.setEnabled(!triggered);
        svSettingDeviceAutoRecord.setAlpha(triggered ? 0.6f : 1.0f);
        svSettingDeviceAutoRecord.setTvSubTitle(triggered ? "" : basePresenter.getAutoRecordTitle(getContext()));

        if (dpStandby != null && !dpStandby.standby) {
            svSettingDeviceLedIndicator.setChecked(dpStandby.led);
        } else {
            svSettingDeviceLedIndicator.setChecked(false);
        }
        svSettingDeviceLedIndicator.setEnabled(!triggered);
        svSettingDeviceLedIndicator.setAlpha(triggered ? 0.6f : 1.0f);
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
