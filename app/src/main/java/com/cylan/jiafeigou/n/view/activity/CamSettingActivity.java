package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityOptionsCompat;
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
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.SettingTip;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.engine.FirmwareCheckerService;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamSettingPresenterImpl;
import com.cylan.jiafeigou.n.view.cam.DeviceInfoDetailFragment;
import com.cylan.jiafeigou.n.view.cam.SafeProtectionFragment;
import com.cylan.jiafeigou.n.view.cam.VideoAutoRecordFragment;
import com.cylan.jiafeigou.n.view.record.DelayRecordActivity;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.SettingItemView1;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_209_LED_INDICATOR;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_501_CAMERA_ALARM_FLAG;
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
    private WeakReference<DeviceInfoDetailFragment> informationWeakReference;
    private WeakReference<VideoAutoRecordFragment> videoAutoRecordFragmentWeakReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_setting);
        ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        if (TextUtils.isEmpty(uuid)) {
            AppLogger.e("uuid is null");
            finishExt();
            return;
        }
        basePresenter = new CamSettingPresenterImpl(this, uuid);
        if (getIntent().getBooleanExtra(JConstant.KEY_JUMP_TO_CAM_DETAIL, false)) {
            jumpDetail(false);
        }
        deviceUpdate(BaseApplication.getAppComponent().getSourceManager().getDevice(this.uuid));
        FirmwareCheckerService.checkVersion(uuid, true);
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
            deviceUpdate(BaseApplication.getAppComponent().getSourceManager().getDevice(uuid));
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
                Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                AlertDialogManager.getInstance().showDialog(this, getString(R.string.SURE_DELETE_1, JFGRules.getDeviceAlias(device)),
                        getString(R.string.SURE_DELETE_1, JFGRules.getDeviceAlias(device)),
                        getString(R.string.OK), (DialogInterface dialogInterface, int i) -> {
                            basePresenter.unbindDevice();
                            LoadingDialog.showLoading(getSupportFragmentManager(), getString(R.string.DELETEING));
                        }, getString(R.string.CANCEL), null);
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
                    deviceUpdate(BaseApplication.getAppComponent().getSourceManager().getDevice(uuid));
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
                    deviceUpdate(BaseApplication.getAppComponent().getSourceManager().getDevice(uuid));
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
                handleJumpToConfig();
                break;
            case R.id.sbtn_setting_sight:
                Intent intent = new Intent(this, SightSettingActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                startActivity(intent);
                break;
        }
    }

    private void handleJumpToConfig() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        if (device == null) {
            finishExt();
            return;
        }
        if (JFGRules.isFreeCam(device.pid)) {
            //freeCam直接进入
            Intent intent = new Intent(this, BindCamActivity.class);
            intent.putExtra(JConstant.JUST_SEND_INFO, uuid);
            startActivity(intent);
        } else {
            DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
            if (!JFGRules.isDeviceOnline(net)) {
                //设备离线
                Intent intent;
                if (JFGRules.isRS(device.pid)) {
                    //特殊设备优先
                    intent = new Intent(this, BindRsCamActivity.class);
                } else {
                    intent = new Intent(this, BindCamActivity.class);
                }
                intent.putExtra(JConstant.JUST_SEND_INFO, uuid);
                startActivity(intent);
            } else {
                //设备在线
                String localSSid = NetUtils.getNetName(ContextUtils.getContext());
                String remoteSSid = net.ssid;
                if (!TextUtils.equals(localSSid, remoteSSid) && net.net == 1) {
                    AlertDialogManager.getInstance().showDialog(this, getString(R.string.setwifi_check, remoteSSid),
                            getString(R.string.setwifi_check, remoteSSid),
                            getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            }, getString(R.string.CANCEL), null);
                } else {
                    //相同ssid,判断为同一个网络环境.太水了.
                    Intent intent = new Intent(this, ConfigWifiActivity_2.class);
                    intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                    startActivity(intent);
                }
            }
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
            if (view.getId() == R.id.sv_setting_device_wifi) {
                continue;
            }
            if (view.getId() == R.id.tv_setting_unbind) {
                continue;//解绑按钮
            }
            if (view.getId() == R.id.sv_setting_device_standby_mode) {
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

    private void initFirmwareHint(Device device) {
        try {
            if (JFGRules.isPanoramicCam(device.pid)) return;
            if (JFGRules.isShareDevice(device)) return;
            String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + getUuid());
            RxEvent.CheckVersionRsp description = new Gson().fromJson(content, RxEvent.CheckVersionRsp.class);
            String currentV = device.$(207, "");
            svSettingDeviceDetail.showRedHint(description.hasNew && BindUtils.versionCompare(description.version, currentV) > 0);
        } catch (Exception e) {
        }
    }

    @Override
    public void deviceUpdate(Device device) {
        initFirmwareHint(device);
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
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
        DpMsgDefine.DPSdStatus sdStatus = device.$(DpMsgMap.ID_204_SDCARD_STORAGE, new DpMsgDefine.DPSdStatus());
        if (sdStatus == null) sdStatus = new DpMsgDefine.DPSdStatus();
        String detailInfo = basePresenter.getDetailsSubTitle(getContext(), sdStatus.hasSdcard, sdStatus.err);
        if (!TextUtils.isEmpty(detailInfo) && detailInfo.contains("(")) {
            svSettingDeviceDetail.setTvSubTitle(detailInfo, android.R.color.holo_red_dark);
        } else
            svSettingDeviceDetail.setTvSubTitle(detailInfo, R.color.color_8C8C8C);
        ////////////////////////standby////////////////////////////////////////////
        DpMsgDefine.DPStandby dpStandby = device.$(DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, new DpMsgDefine.DPStandby());
        if (!JFGRules.showStandbyItem(device.pid)) {
            svSettingDeviceStandbyMode.setVisibility(View.GONE);
        } else {
            svSettingDeviceStandbyMode.setChecked(dpStandby.standby);
            if (!JFGRules.isDeviceOnline(net)) {
                enableStandby(false);//离线
            } else {
                enableStandby(true);
            }
            svSettingDeviceStandbyMode.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                Device device1 = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                DpMsgDefine.DPNet dpNet = device1.$(201, new DpMsgDefine.DPNet());
                if (dpNet.net == -1 && isChecked) {
                    buttonView.setChecked(false);
                    return;
                }
                switchBtn(lLayoutSettingItemContainer, !isChecked);
                DpMsgDefine.DPStandby standby = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid).$(508, new DpMsgDefine.DPStandby());
                standby.standby = isChecked;
                standby.version = System.currentTimeMillis();
                triggerStandby(standby);
                standby.led = isChecked ? ledPreState() : standby.led;//开启待机,保存之前状态到standby.关闭待机,从standby中恢复.
                standby.autoRecord = isChecked ? autoRecordPreState() : standby.autoRecord;
                standby.alarmEnable = isChecked ? alarmPreState() : standby.alarmEnable;
                List<DataPoint> list = new ArrayList<>();
                dpStandby.msgId = 508;
                list.add(dpStandby);
                list.add(new DpMsgDefine.DPPrimary<>(!isChecked && standby.led, ID_209_LED_INDICATOR));
                list.add(new DpMsgDefine.DPPrimary<>(isChecked ? 0 : standby.autoRecord, ID_303_DEVICE_AUTO_VIDEO_RECORD));
                list.add(new DpMsgDefine.DPPrimary<>(!isChecked && standby.alarmEnable, ID_501_CAMERA_ALARM_FLAG));
                basePresenter.updateInfoReq(list);
                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
            });
            switchBtn(lLayoutSettingItemContainer, !dpStandby.standby || net.net > 0);
        }
        /////////////////////////////led/////////////////////////////////////
        if (JFGRules.showLedIndicator(device.pid)) {
            if (!dpStandby.standby) {
                boolean ledTrigger = device.$(209, false);
                svSettingDeviceLedIndicator.setChecked(ledTrigger);
            } else svSettingDeviceLedIndicator.setChecked(false);
            svSettingDeviceLedIndicator.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPStandby standby = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid).$(508, new DpMsgDefine.DPStandby());
                if (standby != null && standby.standby) return;//开启待机模式引起的
                DpMsgDefine.DPPrimary<Boolean> check = new DpMsgDefine.DPPrimary<>();
                check.value = isChecked;
                basePresenter.updateInfoReq(check, ID_209_LED_INDICATOR);
                Log.d("check", "led changed: " + isChecked);
                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
            });
        } else {
            svSettingDeviceLedIndicator.setVisibility(View.GONE);
        }
        ////////////////////////////alarm////////////////////////////////////////
        svSettingSafeProtection.setEnabled(!dpStandby.standby);
        svSettingSafeProtection.setAlpha(!dpStandby.standby ? 1.0f : 0.6f);
        svSettingSafeProtection.setTvSubTitle(dpStandby.standby ? getString(R.string.MAGNETISM_OFF) : basePresenter.getAlarmSubTitle(getContext()));
        ////////////////////////////autoRecord////////////////////////////////////////
        svSettingDeviceAutoRecord.setEnabled(!dpStandby.standby);
        svSettingDeviceAutoRecord.setAlpha(!dpStandby.standby ? 1.0f : 0.6f);
        svSettingDeviceAutoRecord.setTvSubTitle(dpStandby.standby ? "" : basePresenter.getAutoRecordTitle(getContext()));
        ////////////////////////////net////////////////////////////////////////

        boolean isMobileNet = net.net > 1;
        svSettingDeviceWifi.setTvSubTitle(!TextUtils.isEmpty(net.ssid) ? (isMobileNet ? getString(R.string.OFF) : net.ssid) : getString(R.string.OFF_LINE));
        //是否有sim卡
        int simCard = device.$(DpMsgMap.ID_223_MOBILE_NET, 0);
        svSettingDeviceMobileNetwork.setVisibility(JFGRules.isDeviceOnline(net) && JFGRules.showMobileLayout(device.pid) && simCard > 1 ? View.VISIBLE : View.GONE);
        svSettingDeviceMobileNetwork.setEnabled(!dpStandby.standby);
        svSettingDeviceWifi.showDivider(simCard > 1);
        if (JFGRules.is3GCam(device.pid)) {
            boolean s = device.$(DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY, false);
            svSettingDeviceMobileNetwork.setChecked(s);
            svSettingDeviceMobileNetwork.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPPrimary<Boolean> check = new DpMsgDefine.DPPrimary<>();
                check.value = isChecked;
                basePresenter.updateInfoReq(check, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY);
                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
            });
        }
        /////////////////////////////////110v//////////////////////////////////
        if (JFGRules.show110VLayout(device.pid)) {
            boolean state = device.$(216, false);
            sbtnSetting110v.setChecked(state);
            sbtnSetting110v.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPPrimary<Boolean> check = new DpMsgDefine.DPPrimary<>();
                check.value = isChecked;
                basePresenter.updateInfoReq(check, DpMsgMap.ID_216_DEVICE_VOLTAGE);
                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
            });
        } else sbtnSetting110v.setVisibility(View.GONE);

        /////////////////////////旋转/////////////////////////////////////////
        if (JFGRules.isPanoramicCam(device.pid)) {
            svSettingDeviceRotate.setVisibility(View.GONE);
        } else {
            int state = device.$(DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE, 0);
            svSettingDeviceRotate.setChecked(state == 1);
            svSettingDeviceRotate.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPPrimary<Integer> check = new DpMsgDefine.DPPrimary<>();
                check.value = isChecked ? 1 : 0;
                basePresenter.updateInfoReq(check, DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE);
                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
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
        }
        if (JFGRules.isPanoramicCam(device.pid)) {
            sbtnSettingSight.setVisibility(View.VISIBLE);
            try {
                String dpPrimary = device.$(509, "0");
                sbtnSettingSight.setTvSubTitle(getString(TextUtils.equals(dpPrimary, "1") ? R.string.Tap1_Camera_Front : R.string.Tap1_Camera_Overlook));
            } catch (Exception e) {
            }
        } else sbtnSettingSight.setVisibility(View.GONE);
        AppLogger.d(String.format(Locale.getDefault(), "3g?%s,net?%s,", isMobileNet, net));
    }

    @Override
    public void deviceUpdate(JFGDPMsg msg) throws IOException {
        switch ((int) msg.id) {
            case 222:
                if (msg.packValue != null) {
                    DpMsgDefine.DPSdcardSummary summary = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
                    if (summary == null) summary = new DpMsgDefine.DPSdcardSummary();
                    //sd
                    String statusContent = basePresenter.getDetailsSubTitle(getContext(), summary.hasSdcard, summary.errCode);
                    if (!TextUtils.isEmpty(statusContent) && statusContent.contains("(")) {
                        svSettingDeviceDetail.setTvSubTitle(statusContent, android.R.color.holo_red_dark);
                    } else {
                        svSettingDeviceDetail.setTvSubTitle(statusContent, R.color.color_8c8c8c);
                    }
                    //自动录像显示.
                    DpMsgDefine.DPStandby standby = basePresenter.getDevice().$(508, new DpMsgDefine.DPStandby());
                    svSettingDeviceAutoRecord.setTvSubTitle(standby.standby ? "" : basePresenter.getAutoRecordTitle(getContext()));
                }
                break;
            case DpMsgMap.ID_223_MOBILE_NET:
            case ID_209_LED_INDICATOR:
            case DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE:
                deviceUpdate(BaseApplication.getAppComponent().getSourceManager().getDevice(uuid));
                //	0 未知, 1 没卡, 2 user's PIN, 3 user's PUK, 4 network PIN, 5 正常
                break;
        }
    }

    private boolean ledPreState() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        return device.$(ID_209_LED_INDICATOR, false);
    }

    /**
     * 安全防护
     *
     * @return
     */
    private boolean alarmPreState() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        return device.$(ID_501_CAMERA_ALARM_FLAG, false);
    }

    private int autoRecordPreState() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        return device.$(ID_303_DEVICE_AUTO_VIDEO_RECORD, -1);
    }

    private void triggerStandby(DpMsgDefine.DPStandby dpStandby) {
        boolean open = dpStandby.standby;
        svSettingSafeProtection.setEnabled(!open);
        svSettingSafeProtection.setAlpha(open ? 0.6f : 1.0f);
        svSettingSafeProtection.setTvSubTitle(open ? getString(R.string.MAGNETISM_OFF) : basePresenter.getAlarmSubTitle(getContext()));

        svSettingDeviceAutoRecord.setEnabled(!open);
        svSettingDeviceAutoRecord.setAlpha(open ? 0.6f : 1.0f);
        svSettingDeviceAutoRecord.setTvSubTitle(open ? "" : basePresenter.getAutoRecordTitle(getContext()));

        boolean led = !open && dpStandby.led;
        svSettingDeviceLedIndicator.setEnabled(!open);
        svSettingDeviceLedIndicator.setChecked(!open && led);
        svSettingDeviceLedIndicator.setAlpha(open ? 0.6f : 1.0f);
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
        DpMsgDefine.DPStandby dpStandby = basePresenter.getDevice().$(DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, new DpMsgDefine.DPStandby());
        svSettingDeviceMobileNetwork.setEnabled(!dpStandby.standby && connected);
        svSettingDeviceDelayCapture.setEnabled(connected);

        if (!connected) {
            svSettingDeviceMobileNetwork.setAlpha(0.6f);
            svSettingDeviceDelayCapture.setAlpha(0.6f);

        } else {
            svSettingDeviceMobileNetwork.setAlpha(1.0f);
            svSettingDeviceDelayCapture.setAlpha(1.0f);
        }
        DpMsgDefine.DPNet net = basePresenter.getDevice().$(201, new DpMsgDefine.DPNet());
        enableStandby(connected && JFGRules.isDeviceOnline(net));
    }

    private void enableStandby(boolean enable) {
        svSettingDeviceStandbyMode.setEnabled(enable);
        svSettingDeviceStandbyMode.setAlpha(!enable ? 0.6f : 1.0f);
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
