package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.DeviceInfoDetailPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.dialog.EditFragmentDialog;

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
    @BindView(R.id.tv_device_alias)
    TextView tvDeviceAlias;
    @BindView(R.id.lLayout_information_facility_name)
    LinearLayout lLayoutInformationFacilityName;
    @BindView(R.id.tv_device_time_zone)
    TextView tvDeviceTimeZone;
    @BindView(R.id.lLayout_information_facility_timezone)
    LinearLayout lLayoutInformationFacilityTimezone;
    @BindView(R.id.tv_device_sdcard_state)
    TextView tvDeviceSdcardState;
    @BindView(R.id.tv_device_mobile_net)
    TextView tvDeviceMobileNet;
    @BindView(R.id.tv_device_wifi_state)
    TextView tvDeviceWifiState;
    @BindView(R.id.tv_device_cid)
    TextView tvDeviceCid;
    @BindView(R.id.tv_device_mac)
    TextView tvDeviceMac;
    @BindView(R.id.tv_device_system_version)
    TextView tvDeviceSystemVersion;
    @BindView(R.id.tv_device_battery_level)
    TextView tvDeviceBatteryLevel;
    @BindView(R.id.lLayout_device_battery)
    LinearLayout lLayoutDeviceBattery;
    @BindView(R.id.tv_device_uptime)
    TextView tvDeviceUptime;
    @BindView(R.id.ll_SDcard_item)
    LinearLayout llSDcardItem;
    @BindView(R.id.rl_hardware_update)
    RelativeLayout rlHardwareUpdate;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.hardware_update_point)
    View hardwareUpdatePoint;
    @BindView(R.id.tv_new_software)
    TextView tvNewSoftware;
    private String uuid;
    private EditFragmentDialog editDialogFragment;
    private RxEvent.CheckDevVersionRsp checkDevVersion;

    public static DeviceInfoDetailFragment newInstance(Bundle bundle) {
        DeviceInfoDetailFragment fragment = new DeviceInfoDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
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
        JFGDevice device = GlobalDataProxy.getInstance().fetch(this.uuid);
        //仅3G摄像头、FreeCam显示此栏
        if (device != null && (JFGRules.isFreeCam(device.pid) || JFGRules.is3GCam(device.pid))) {
            lLayoutDeviceBattery.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateDetails();
        basePresenter.checkNewSoftVersion();
    }

    private void updateDetails() {
        MiscUtils.loadTimeZoneList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((List<TimeZoneBean> list) -> {
                    DpMsgDefine.DPTimeZone zone = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_214_DEVICE_TIME_ZONE, DpMsgDefine.DPTimeZone.empty);
                    TimeZoneBean bean = new TimeZoneBean();
                    bean.setId(zone.timezone);
                    if (list != null) {
                        int index = list.indexOf(bean);
                        if (index > 0 && index < list.size()) {
                            tvDeviceTimeZone.setText(list.get(index).getName());
                        }
                    }
                });
        DpMsgDefine.DPSdStatus status = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE, null);
        tvDeviceSdcardState.setText(getSdcardState(status));
        JFGDevice device = GlobalDataProxy.getInstance().fetch(uuid);
        tvDeviceAlias.setText(device == null ? "" : TextUtils.isEmpty(device.alias) ? uuid : device.alias);
        tvDeviceCid.setText(uuid);
        String mac = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_202_MAC, "");
        tvDeviceMac.setText(mac);
        int battery = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_206_BATTERY, 0);
        tvDeviceBatteryLevel.setText(String.format(Locale.getDefault(), "%s", battery));
        String sVersion = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_208_DEVICE_SYS_VERSION, "");
        tvDeviceSystemVersion.setText(sVersion);
        int uptime = GlobalDataProxy.getInstance().getValue(this.uuid, DpMsgMap.ID_210_UP_TIME, 0);
        tvDeviceUptime.setText(TimeUtils.getUptime(uptime));
    }

    private String getSdcardState(DpMsgDefine.DPSdStatus sdStatus) {
        //sd卡状态
        if (sdStatus != null) {
            if (!sdStatus.hasSdcard && sdStatus.err != 0) {
                //sd初始化失败时候显示
                return getString(R.string.NO_SDCARD);
            }
        }
        if (sdStatus != null && !sdStatus.hasSdcard) {
            return getString(R.string.SD_ERR_1);
        }
        return sdStatus != null ? getString(R.string.SD_NORMAL) : "";
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.lLayout_information_facility_name, R.id.lLayout_information_facility_timezone, R.id.ll_SDcard_item, R.id.rl_hardware_update})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().onBackPressed();
                break;
            case R.id.lLayout_information_facility_name:
                toEditAlias();
                break;
            case R.id.lLayout_information_facility_timezone:
                toEditTimezone();
                break;
            case R.id.ll_SDcard_item:
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
        bundle.putSerializable("version_content",checkDevVersion);
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
            updateDetails();
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
        editDialogFragment.setAction(new EditFragmentDialog.DialogAction<String>() {
            @Override
            public void onDialogAction(int id, String value) {
                JFGDevice device = GlobalDataProxy.getInstance().fetch(uuid);
                if (!TextUtils.isEmpty(value) && device != null && !TextUtils.equals(value, device.alias)) {
                    device.alias = value;
                    tvDeviceAlias.setText(value);
                    GlobalDataProxy.getInstance().updateJFGDevice(device);
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
        checkDevVersion = checkDevVersionRsp;
        if (checkDevVersionRsp.hasNew) {
            hardwareUpdatePoint.setVisibility(View.VISIBLE);
            tvNewSoftware.setVisibility(View.VISIBLE);
        }
    }
}
