package com.cylan.jiafeigou.n.view.panorama;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.activity.BindPanoramaCamActivity;
import com.cylan.jiafeigou.n.view.cam.DeviceInfoDetailFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;
import static com.cylan.jiafeigou.utils.ActivityUtils.loadFragment;

/**
 * Created by yanzhendong on 2017/3/15.
 */

public class PanoramaSettingActivity extends BaseActivity<PanoramaSettingContact.Presenter> implements PanoramaSettingContact.View {
    @BindView(R.id.fLayout_top_bar_container)
    CustomToolbar toolbarContainer;
    @BindView(R.id.sv_setting_device_detail)
    SettingItemView0 deviceDetail;
    @BindView(R.id.sv_setting_device_wifi)
    SettingItemView0 svSettingDeviceWifi;
    @BindView(R.id.sv_setting_device_mode)
    SettingItemView0 svSettingDeviceMode;


    @Override
    public void onStart() {
        super.onStart();
        attributeUpdate();
    }

    @Override
    public void onStop() {
        super.onStop();
    }



    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        deviceDetail.setSubTitle(TextUtils.isEmpty(device.alias) ? device.uuid : device.alias);
        deviceDetail.showRedHint(!TextUtils.isEmpty(PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + uuid)));
        toolbarContainer.setBackAction(this::exit);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_panorama_setting;
    }

    public void exit(View view) {
        onBackPressed();
    }

    @OnClick(R.id.sv_setting_device_detail)
    public void showDeviceDetail() {
        DeviceInfoDetailFragment fragment = DeviceInfoDetailFragment.newInstance(null);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
        bundle.putBoolean(JConstant.KEY_SHOW_TIME_ZONE, false);
        fragment.setArguments(bundle);
        loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
    }

    @OnClick(R.id.sv_setting_device_logo)
    public void showDeviceLogoConfigure() {
        AppLogger.d("打开logo 选择页面");
        PanoramaLogoConfigureFragment fragment = PanoramaLogoConfigureFragment.newInstance();
        loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
    }

    @OnClick(R.id.sv_setting_device_wifi)
    public void showDeviceFamliySetting() {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        if (device == null) {
            finish();
            return;
        }
        Intent intent = new Intent(this, BindPanoramaCamActivity.class);
        intent.putExtra("PanoramaConfigure", "Family");
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        startActivity(intent);
    }

    @OnClick(R.id.sv_setting_device_mode)
    public void showDeviceOutDoorSetting() {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        if (device == null) {
            finish();
            return;
        }
        Intent intent = new Intent(this, BindPanoramaCamActivity.class);
        intent.putExtra("PanoramaConfigure", "OutDoor");
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        startActivity(intent);
    }

    @OnClick(R.id.tv_setting_unbind)
    public void unBindDevice() {
        if (NetUtils.getJfgNetType() == 0) {
            ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
            return;
        }
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        AlertDialogManager.getInstance().showDialog(this, getString(R.string.SURE_DELETE_1, JFGRules.getDeviceAlias(device)),
                getString(R.string.SURE_DELETE_1, JFGRules.getDeviceAlias(device)),
                getString(R.string.OK), (DialogInterface dialogInterface, int i) -> {
                    presenter.unBindDevice();
                    LoadingDialog.showLoading(this, getString(R.string.DELETEING), true);
                }, getString(R.string.CANCEL), null, false);
    }

    @Override
    public void unbindDeviceRsp(int resultCode) {
        AppLogger.d("unbindDeviceRsp: " + resultCode);
        if (resultCode == JError.ErrorOK) {
            LoadingDialog.dismissLoading();
            ToastUtil.showPositiveToast(getString(R.string.DELETED_SUC));
            startActivity(new Intent(this, NewHomeActivity.class));//回到主页
        }
    }

    @Override
    public void attributeUpdate() {
        Device mDevice = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        boolean isAp = JFGRules.isAPDirect(mDevice.uuid, mDevice.$(202, ""));
        if (isAp) {
            svSettingDeviceWifi.setSubTitle(getString(R.string.Tap1_Setting_Unopened));
            svSettingDeviceMode.setSubTitle(getString(R.string.Tap1_OutdoorMode_Opened));
        } else {
            svSettingDeviceMode.setSubTitle(getString(R.string.Tap1_Setting_Unopened));
            DpMsgDefine.DPNet net = mDevice.$(201, new DpMsgDefine.DPNet());
            if (JFGRules.isDeviceOnline(net)) {
                svSettingDeviceWifi.setSubTitle(net.ssid);
            } else {
                svSettingDeviceWifi.setSubTitle(getString(R.string.Tap1_Setting_Unopened));
            }
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
