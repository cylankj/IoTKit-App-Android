package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.DeviceInfoDetailPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.dialog.EditFragmentDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_LEFT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_RIGHT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TITLE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TOUCH_OUT_SIDE_DISMISS;


/**
 * 创建者     谢坤
 * 创建时间   2016/7/12 17:53
 * 用来控制摄像头模块下的设备信息，点击设备名称和设备时区时进行切换
 */
public class DeviceInfoDetailFragment extends IBaseFragment<CamInfoContract.Presenter>
        implements CamInfoContract.View {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.tv_device_alias)
    SettingItemView0 tvDeviceAlias;
    @BindView(R.id.tv_device_time_zone)
    SettingItemView0 tvDeviceTimeZone;
    @BindView(R.id.tv_device_sdcard_state)
    SettingItemView0 tvDeviceSdcardState;
    @BindView(R.id.tv_device_mobile_net)
    SettingItemView0 tvDeviceMobileNet;
    @BindView(R.id.tv_device_wifi_state)
    SettingItemView0 tvDeviceWifiState;
    @BindView(R.id.tv_device_mac)
    SettingItemView0 tvDeviceMac;
    @BindView(R.id.tv_device_system_version)
    SettingItemView0 tvDeviceSystemVersion;
    @BindView(R.id.tv_device_battery_level)
    SettingItemView0 tvDeviceBatteryLevel;
    @BindView(R.id.tv_device_uptime)
    SettingItemView0 tvDeviceUptime;

    @BindView(R.id.rl_hardware_update)
    SettingItemView0 rlHardwareUpdate;

    //    @BindView(R.dpMsgId.hardware_update_point)
//    View hardwareUpdatePoint;
//    @BindView(R.dpMsgId.tv_new_software)
//    TextView tvNewSoftware;
    @BindView(R.id.tv_device_cid)
    SettingItemView0 tvDeviceCid;
    private String uuid;
    private EditFragmentDialog editDialogFragment;
    private RxEvent.CheckDevVersionRsp checkDevVersion;
    private Device device;

    public static DeviceInfoDetailFragment newInstance(Bundle bundle) {
        DeviceInfoDetailFragment fragment = new DeviceInfoDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
        device = DataSourceManager.getInstance().getJFGDevice(uuid);
        basePresenter = new DeviceInfoDetailPresenterImpl(this, uuid);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facility_information, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean showBattery = JFGRules.showBatteryItem(device != null ? device.pid : 0);
        //仅3G摄像头、FreeCam显示此栏
        tvDeviceBatteryLevel.setVisibility(showBattery ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateDetails();
        basePresenter.checkNewSoftVersion();
    }

    private void updateDetails() {
        //是否分享设备
        if (device != null && !TextUtils.isEmpty(device.shareAccount)) {
            tvDeviceAlias.showDivider(false);
            tvDeviceTimeZone.setVisibility(View.GONE);
            tvDeviceSdcardState.setVisibility(View.GONE);
            rlHardwareUpdate.setVisibility(View.GONE);
            getView().findViewById(R.id.tv_storage).setVisibility(View.GONE);
        }
        //是否显示移动网络
        tvDeviceMobileNet.setVisibility(device != null && JFGRules.showMobileLayout(device.pid) ? View.VISIBLE : View.GONE);
        DpMsgDefine.DPTimeZone zone = MiscUtils.safeGet_(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_214_DEVICE_TIME_ZONE), DpMsgDefine.EMPTY.TIME_ZONE);
        if (zone != null)
            MiscUtils.loadTimeZoneList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((List<TimeZoneBean> list) -> {
                        TimeZoneBean bean = new TimeZoneBean();
                        bean.setId(zone.timezone);
                        if (list != null) {
                            int index = list.indexOf(bean);
                            if (index >= 0 && index < list.size()) {
                                tvDeviceTimeZone.setTvSubTitle(list.get(index).getName());
                            }
                        }
                    });
        DpMsgDefine.DPSdStatus status = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE);
        String statusContent = getSdcardState(status);
        if (!TextUtils.isEmpty(statusContent) && statusContent.contains("(")) {
            tvDeviceSdcardState.setTvSubTitle(statusContent, android.R.color.holo_red_dark);
        } else {
            tvDeviceSdcardState.setTvSubTitle(statusContent, R.color.color_8c8c8c);
        }
        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
        tvDeviceAlias.setTvSubTitle(device == null ? "" : TextUtils.isEmpty(device.alias) ? uuid : device.alias);
        tvDeviceCid.setTvSubTitle(uuid);
        DpMsgDefine.DPPrimary<String> mac = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_202_MAC);
        String m = MiscUtils.safeGet(mac, "");
        tvDeviceMac.setTvSubTitle(m);
        DpMsgDefine.DPPrimary<Integer> battery = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_206_BATTERY);
        int b = MiscUtils.safeGet(battery, 0);
        tvDeviceBatteryLevel.setTvSubTitle(String.format(Locale.getDefault(), "%s", b));
        DpMsgDefine.DPPrimary<String> sVersion = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_208_DEVICE_SYS_VERSION);
        String v = MiscUtils.safeGet(sVersion, "");
        tvDeviceSystemVersion.setTvSubTitle(v);
        DpMsgDefine.DPPrimary<Integer> uptime = DataSourceManager.getInstance().getValue(this.uuid, DpMsgMap.ID_210_UP_TIME);
        int u = MiscUtils.safeGet(uptime, 0);
        tvDeviceUptime.setTvSubTitle(TimeUtils.getUptime(u));
        DpMsgDefine.DPNet net = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_201_NET);
        tvDeviceWifiState.setTvSubTitle(net != null && !TextUtils.isEmpty(net.ssid) ? net.ssid : getString(R.string.OFF_LINE));
    }

    private String getSdcardState(DpMsgDefine.DPSdStatus sdStatus) {
        //sd卡状态
        if (sdStatus != null) {
            if (sdStatus.hasSdcard && sdStatus.err != 0) {
                //sd初始化失败时候显示
                return getString(R.string.SD_INIT_ERR, sdStatus.err);
            }
        }
        if (sdStatus != null && !sdStatus.hasSdcard) {
            return getString(R.string.SD_NO);
        }
        return sdStatus != null ? getString(R.string.SD_NORMAL) : "";
    }

    @OnClick({R.id.tv_toolbar_icon,
            R.id.tv_device_sdcard_state,
            R.id.tv_device_alias,
            R.id.tv_device_time_zone,
            R.id.rl_hardware_update})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().onBackPressed();
                break;
            case R.id.tv_device_alias:
                toEditAlias();
                break;
            case R.id.tv_device_time_zone:
                toEditTimezone();
                break;
            case R.id.tv_device_sdcard_state:
                DpMsgDefine.DPSdStatus status = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE);
                if (status == null) {
                    return;
                }
                String statusContent = getSdcardState(status);
                if (!TextUtils.isEmpty(statusContent) && statusContent.contains("(")) {
                    showClearSDDialog();
                    return;
                }

                if (status.hasSdcard)//没有sd卡,不能点击
                    jump2SdcardDetailFragment();
                break;
            case R.id.rl_hardware_update:
                jump2HardwareUpdateFragment();
                break;
        }
    }

    /**
     * 固件升级
     */
    private void jump2HardwareUpdateFragment() {
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        bundle.putSerializable("version_content", checkDevVersion);
        HardwareUpdateFragment hardwareUpdateFragment = HardwareUpdateFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                hardwareUpdateFragment, android.R.id.content);
    }

    /**
     * 显示Sd卡的详情
     */
    private void jump2SdcardDetailFragment() {

        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        SDcardDetailFragment sdcardDetailFragment = SDcardDetailFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                sdcardDetailFragment, android.R.id.content);
    }

    /**
     * 格式化SD卡
     */
    private void showClearSDDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.SD_INIT));
        bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
        bundle.putString(SimpleDialogFragment.KEY_CONTENT_CONTENT, getString(R.string.VIDEO_SD_DESC));
        SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.newInstance(bundle);
        simpleDialogFragment.setAction((int id, Object value) -> {
            //开始格式化
            basePresenter.clearSdcard();
            showLoading();
        });
        simpleDialogFragment.show(getFragmentManager(), "simpleDialogFragment");
    }


    /**
     * 编辑时区
     */
    private void toEditTimezone() {
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        DeviceTimeZoneFragment timeZoneFragment = DeviceTimeZoneFragment.newInstance(bundle);
        timeZoneFragment.setCallBack((Object o) -> {
            if (!(o instanceof DpMsgDefine.DPTimeZone)) {
                return;
            }
            //更新ui
            DpMsgDefine.DPTimeZone zone = MiscUtils.safeGet_(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_214_DEVICE_TIME_ZONE), DpMsgDefine.EMPTY.TIME_ZONE);
            if (zone != null)
                MiscUtils.loadTimeZoneList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((List<TimeZoneBean> list) -> {
                            TimeZoneBean bean = new TimeZoneBean();
                            bean.setId(zone.timezone);
                            if (list != null) {
                                int index = list.indexOf(bean);
                                if (index >= 0 && index < list.size()) {
                                    tvDeviceTimeZone.setTvSubTitle(list.get(index).getName());
                                }
                            }
                        });
        });
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                timeZoneFragment, android.R.id.content);
    }

    /**
     * 编辑昵称
     */
    private void toEditAlias() {
        if (editDialogFragment == null) {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_TITLE, getString(R.string.EQUIPMENT_NAME));
            bundle.putString(KEY_LEFT_CONTENT, getString(R.string.OK));
            bundle.putString(KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
            bundle.putBoolean(KEY_TOUCH_OUT_SIDE_DISMISS, false);
            editDialogFragment = EditFragmentDialog.newInstance(bundle);
        }
        if (editDialogFragment.isVisible())
            return;
        editDialogFragment.show(getChildFragmentManager(), "editDialogFragment");
        editDialogFragment.setAction((int id, Object value) -> {
            if (value != null && value instanceof String) {
                String content = (String) value;
                Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
                if (!TextUtils.isEmpty(content) && device != null && !TextUtils.equals(content, device.alias)) {
                    device.alias = content;
                    tvDeviceAlias.setTvSubTitle((CharSequence) value);
                    if (basePresenter != null) basePresenter.updateAlias(device);
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (callBack != null)
            callBack.callBack(null);
    }

    @Override
    public void setPresenter(CamInfoContract.Presenter presenter) {
        basePresenter = presenter;
    }

    @Override
    public void checkDevResult(RxEvent.CheckDevVersionRsp checkDevVersionRsp) {
        DpMsgDefine.DPPrimary<String> sVersion = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_207_DEVICE_VERSION);
        String s = MiscUtils.safeGet(sVersion, "");
        checkDevVersion = checkDevVersionRsp;
        rlHardwareUpdate.setTvSubTitle(checkDevVersionRsp.hasNew ? getString(R.string.Tap1_NewFirmware) : s);
        rlHardwareUpdate.showRedHint(checkDevVersionRsp.hasNew);
    }

    @Override
    public void showLoading() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.SD_INFO_2));
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    @Override
    public void clearSdResult(int code) {
        hideLoading();
        if (code == 0) {
            ToastUtil.showPositiveToast(getString(R.string.SD_INFO_3));
        } else {
            ToastUtil.showNegativeToast(getString(R.string.SD_ERR_3));
        }
    }

    @Override
    public void setAliasRsp(int code) {
        if (code == JError.ErrorOK)
            ToastUtil.showPositiveToast(getString(R.string.SCENE_SAVED));
        else ToastUtil.showNegativeToast(getString(R.string.set_failed));
    }

    @Override
    public void deviceUpdate(Device device) {
        //sd
        DpMsgDefine.DPSdStatus status = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE);
        String statusContent = getSdcardState(status);
        if (!TextUtils.isEmpty(statusContent) && statusContent.contains("(")) {
            tvDeviceSdcardState.setTvSubTitle(statusContent, android.R.color.holo_red_dark);
        } else {
            tvDeviceSdcardState.setTvSubTitle(statusContent, R.color.color_8c8c8c);
        }
        //zone
        MiscUtils.loadTimeZoneList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((List<TimeZoneBean> list) -> {
                    DpMsgDefine.DPTimeZone zone = MiscUtils.safeGet_(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_214_DEVICE_TIME_ZONE), DpMsgDefine.EMPTY.TIME_ZONE);
                    if (zone == null) return;
                    TimeZoneBean bean = new TimeZoneBean();
                    bean.setId(zone.timezone);
                    if (list != null) {
                        int index = list.indexOf(bean);
                        if (index >= 0 && index < list.size()) {
                            tvDeviceTimeZone.setTvSubTitle(list.get(index).getName());
                        }
                    }
                }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
        //wifi
        DpMsgDefine.DPNet net = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_201_NET);
        tvDeviceWifiState.setTvSubTitle(net != null && !TextUtils.isEmpty(net.ssid) ? net.ssid : getString(R.string.OFF_LINE));
    }
}
