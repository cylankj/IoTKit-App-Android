package com.cylan.jiafeigou.n.view.bell;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDoorBellDevice;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellDetailSettingPresenterImpl;
import com.cylan.jiafeigou.n.view.cam.HardwareUpdateFragment;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.EditFragmentDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_DEFAULT_EDIT_TEXT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_LEFT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_RIGHT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TITLE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TOUCH_OUT_SIDE_DISMISS;

public class BellDetailFragment extends BaseFragment<BellDetailContract.Presenter>
        implements BellDetailContract.View,
        BaseDialog.BaseDialogAction {

    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
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

    private RxEvent.CheckDevVersionRsp checkDevVersion;

    public static BellDetailFragment newInstance(Bundle bundle) {
        BellDetailFragment fragment = new BellDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected BellDetailContract.Presenter onCreatePresenter() {
        return new BellDetailSettingPresenterImpl();
    }

    @Override
    protected int getContentViewID() {
        return R.layout.layout_fragment_bell_detail_setting;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initTopBar();
    }

    private void initTopBar() {
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }

    @Override
    public void onDialogAction(int id, Object value) {

    }

    private EditFragmentDialog editDialogFragment;

    @OnClick(R.id.sv_setting_device_alias)
    public void onClick() {
        if (editDialogFragment == null) {
            editDialogFragment = EditFragmentDialog.newInstance(null);
        }
        if (editDialogFragment.isVisible())
            return;
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
                Device device = DataSourceManager.getInstance().getJFGDevice(mUUID);
                if (!TextUtils.isEmpty(content)
                        && device != null && !TextUtils.equals(device.alias, content)) {
                    device.alias = content;
                    svSettingDeviceAlias.setTvSubTitle(content);
                    DataSourceManager.getInstance().updateJFGDevice(device);
                }
            }
        });
    }

    @Override
    public String onResolveViewLaunchType() {
        return null;
    }


    @Override
    public void onShowProperty(JFGDoorBellDevice device) {
        String alias = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
        svSettingDeviceAlias.setTvSubTitle(alias);
        svSettingDeviceCid.setTvSubTitle(device.uuid);
        svSettingDeviceMac.setTvSubTitle(device.mac.value);
        svSettingDeviceSysVersion.setTvSubTitle(device.device_sys_version.value);
        svSettingDeviceVersion.setTvSubTitle(device.device_version.value);
        int battery = MiscUtils.safeGet(device.battery, 0);
        svSettingDeviceBattery.setTvSubTitle(battery + "");
        DpMsgDefine.DPNet net = MiscUtils.safeGet_(device.net, DpMsgDefine.DPNet.empty);
        String ssid = TextUtils.isEmpty(net.ssid) ? getString(R.string.OFF_LINE) : net.ssid;
        svSettingDeviceWifi.setTvSubTitle(ssid);
        svSettingDeviceUptime.setTvSubTitle(TimeUtils.getUptime(MiscUtils.safeGet(device.up_time, 0)));
        hardwareUpdatePoint.setVisibility(View.INVISIBLE);
        svSettingHardwareUpdate.setTvSubTitle(MiscUtils.safeGet(device.device_version, ""));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @OnClick(R.id.rl_hardware_update)
    public void OnClick() {
        if (checkDevVersion != null) {
            Bundle bundle = new Bundle();
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, mUUID);
            bundle.putSerializable("version_content", checkDevVersion);
            HardwareUpdateFragment hardwareUpdateFragment = HardwareUpdateFragment.newInstance(bundle);
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                    hardwareUpdateFragment, android.R.id.content);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPresenter != null)
            mPresenter.checkNewVersion(mUUID);
    }

    @Override
    public void checkResult(RxEvent.CheckDevVersionRsp checkDevVersionRsp) {
        this.checkDevVersion = checkDevVersionRsp;
        if (checkDevVersionRsp.hasNew) {
            hardwareUpdatePoint.setVisibility(View.VISIBLE);
            svSettingHardwareUpdate.setTvSubTitle("新固件");
        }
    }
}
