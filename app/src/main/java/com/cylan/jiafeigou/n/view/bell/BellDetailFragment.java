package com.cylan.jiafeigou.n.view.bell;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cylan.ex.JfgException;
import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellDetailContract;
import com.cylan.jiafeigou.n.view.firmware.FirmwareUpdateActivity;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.EditFragmentDialog;
import com.google.gson.Gson;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_201_NET;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_202_MAC;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_206_BATTERY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_207_DEVICE_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_208_DEVICE_SYS_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_210_UP_TIME;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_DEFAULT_EDIT_TEXT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_LEFT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_RIGHT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TITLE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TOUCH_OUT_SIDE_DISMISS;

public class BellDetailFragment extends BaseFragment<BellDetailContract.Presenter>
        implements BellDetailContract.View,
        BaseDialog.BaseDialogAction {

    @BindView(R.id.sv_setting_device_alias)
    SettingItemView0 svSettingDeviceAlias;
    @BindView(R.id.sv_setting_device_cid)
    SettingItemView0 svSettingDeviceCid;
    @BindView(R.id.sv_setting_device_wifi)
    SettingItemView0 svSettingDeviceWifi;
    @BindView(R.id.sv_setting_device_mac)
    SettingItemView0 svSettingDeviceMac;
    @BindView(R.id.sv_setting_device_sys_version)
    SettingItemView0 svSettingDeviceSysVersion;
    @BindView(R.id.sv_setting_device_version)
    SettingItemView0 svSettingDeviceVersion;
    @BindView(R.id.sv_setting_device_battery)
    SettingItemView0 svSettingDeviceBattery;
    @BindView(R.id.sv_setting_device_uptime)
    SettingItemView0 svSettingDeviceUptime;
    @BindView(R.id.lLayout_setting_container)
    LinearLayout lLayoutSettingContainer;
    @BindView(R.id.sv_setting_hardware_update)
    SettingItemView0 svSettingHardwareUpdate;
    @BindView(R.id.hardware_update_point)
    View hardwareUpdatePoint;
    @BindView(R.id.rl_hardware_update)
    RelativeLayout rlHardwareUpdate;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    private RxEvent.CheckVersionRsp checkDevVersion;
    @Inject
    JFGSourceManager sourceManager;
    @Inject
    AppCmd appCmd;

    public static BellDetailFragment newInstance(Bundle bundle) {
        BellDetailFragment fragment = new BellDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onDialogAction(int id, Object value) {

    }

    private EditFragmentDialog editDialogFragment;

    @OnClick(R.id.sv_setting_device_alias)
    public void onClick() {
        editDialogFragment = EditFragmentDialog.newInstance(null);
        editDialogFragment.setDefaultFilter(new InputFilter[]{
                new InputFilter.LengthFilter(24)
        });
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, getString(R.string.EQUIPMENT_NAME));
        bundle.putString(KEY_LEFT_CONTENT, getString(R.string.OK));
        bundle.putString(KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
        bundle.putBoolean(KEY_TOUCH_OUT_SIDE_DISMISS, false);
        bundle.putString(KEY_DEFAULT_EDIT_TEXT, svSettingDeviceAlias.getSubTitle().toString());
        editDialogFragment.setArguments(bundle);
        editDialogFragment.show(getChildFragmentManager(), "editDialogFragment");
        editDialogFragment.setAction((id, value) -> {
            if (value != null && value instanceof String) {
                String content = (String) value;
                Device device = sourceManager.getDevice(uuid);
                if (!TextUtils.isEmpty(content)
                        && device != null && !TextUtils.equals(device.alias, content)) {
                    device.alias = content;
                    svSettingDeviceAlias.setSubTitle(content);
                    HandlerThreadUtils.post(() -> {
                        updateAlias(device);
                    });
                }
            }
        });
    }


    public void updateAlias(Device device) {
        sourceManager.updateDevice(device);
        try {
            appCmd.setAliasByCid(device.uuid, device.alias);
            AppLogger.w("update alias suc");
        } catch (JfgException e) {
            AppLogger.e("err: set up remote alias failed: " + new Gson().toJson(device));
        }
    }


    @Override
    public void onShowProperty(Device device) {
        String alias = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
        svSettingDeviceAlias.setSubTitle(alias);
        svSettingDeviceCid.setSubTitle(device.uuid);
        svSettingDeviceMac.setSubTitle(device.$(ID_202_MAC, ""));
        svSettingDeviceSysVersion.setSubTitle(device.$(ID_208_DEVICE_SYS_VERSION, ""));
        svSettingDeviceVersion.setSubTitle(device.$(ID_207_DEVICE_VERSION, ""));
        int battery = device.$(ID_206_BATTERY, 0);
        svSettingDeviceBattery.setSubTitle(battery + "%");
        DpMsgDefine.DPNet net = device.$(ID_201_NET, new DpMsgDefine.DPNet());
        String ssid = TextUtils.isEmpty(net.ssid) ? getString(R.string.OFF_LINE) : net.ssid;
        svSettingDeviceWifi.setSubTitle(ssid);
        if (net.net > 0) {
            svSettingDeviceUptime.setSubTitle(TimeUtils.getUptime(device.$(ID_210_UP_TIME, 0)));
        } else {
            svSettingDeviceUptime.setVisibility(View.GONE);
        }
        hardwareUpdatePoint.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(device.shareAccount)) {
            rlHardwareUpdate.setVisibility(View.GONE);
        } else {
            svSettingHardwareUpdate.setSubTitle(device.$(ID_207_DEVICE_VERSION, ""));
            if (presenter != null) {
                presenter.checkNewVersion(uuid);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_bell_detail_setting, container, false);
    }


    @OnClick({R.id.tv_toolbar_icon, R.id.rl_hardware_update})
    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getFragmentManager().popBackStack();
                break;
            case R.id.rl_hardware_update:
//                if (checkDevVersion != null) {
                Intent intent = new Intent(getActivity(), FirmwareUpdateActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                getActivity().startActivity(intent);
//                }
                break;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void checkResult(RxEvent.CheckVersionRsp checkDevVersionRsp) {
        this.checkDevVersion = checkDevVersionRsp;
        if (checkDevVersionRsp.hasNew) {
            hardwareUpdatePoint.setVisibility(View.VISIBLE);
            svSettingHardwareUpdate.setSubTitle(getString(R.string.Tap1_NewFirmware));
        }
    }
}
