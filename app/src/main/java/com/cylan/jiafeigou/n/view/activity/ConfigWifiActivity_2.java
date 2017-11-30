package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.ConfigApPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.LocalWifiInfo;
import com.cylan.jiafeigou.n.view.bind.WiFiListDialogFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.dialog.EditFragmentDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;
import static com.cylan.jiafeigou.widget.dialog.BaseDialog.KEY_TITLE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_DEFAULT_EDIT_TEXT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_EXCLUDE_CHINESE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_INPUT_HINT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_INPUT_LENGTH;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_LEFT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_RIGHT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_SHOW_EDIT;

/**
 * 就一个wifi配置,非要搞两套设计.fxx.
 */
public class ConfigWifiActivity_2 extends BaseBindActivity<ConfigApContract.Presenter>
        implements ConfigApContract.View, WiFiListDialogFragment.ClickCallBack,
        OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.rv_wifi_list)
    RecyclerView rvWifiList;
    @BindView(R.id.sw_refresh_wifi)
    SwipeRefreshLayout swRefreshWifi;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_wifi_2);
        ButterKnife.bind(this);
        this.uuid = getIntent().getStringExtra(KEY_DEVICE_ITEM_UUID);
        this.presenter = new ConfigApPresenterImpl(this);
        rvWifiList.setAdapter(new AAdapter(getContext(), null, R.layout.layout_wifi_list_item));
        ((AAdapter) rvWifiList.getAdapter()).setOnItemClickListener(this);
        rvWifiList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        swRefreshWifi.setOnRefreshListener(this);
        swRefreshWifi.setColorSchemeColors(Color.parseColor("#36BDFF"));
        customToolbar.setBackAction(v -> {//回退事件
            onBackPressed();
        });
    }

    @Override
    public void onNetStateChanged(int state) {

    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.refreshWifiList();
        }
    }

    @Override
    public void onWiFiResult(List<ScanResult> resultList) {
        ((AAdapter) rvWifiList.getAdapter()).clear();
        ((AAdapter) rvWifiList.getAdapter()).addAll(resultList);
        swRefreshWifi.setRefreshing(false);
    }

    @Override
    public void onSetWifiFinished(UdpConstant.UdpDevicePortrait udpDevicePortrait) {

    }

    @Override
    public void sendWifiInfoFailed() {
        LoadingDialog.dismissLoading();
//        ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_4));
    }

    @Override
    public void check3gFinish() {

    }

    @Override
    public void upgradeDogState(int state) {

    }

    @Override
    public void pingFailed() {

    }

    @Override
    public void onDeviceAlreadyExist() {

    }

    @Override
    public void onAccessLocationPermissionRejected() {
        AppLogger.d("onAccessLocationPermissionRejected");
    }

    @Override
    public void onDismiss(ScanResult scanResult) {

    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {
        final ScanResult item = ((AAdapter) rvWifiList.getAdapter()).getItem(position);
        LocalWifiInfo.Saver.getSaver().getInfo(item.SSID.replace("\"", ""))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(info -> {
                    Bundle bundle = new Bundle();
                    final String ssid = item.SSID.replace("\"", "");
                    bundle.putString(KEY_TITLE, ssid);
                    bundle.putString(KEY_LEFT_CONTENT, getString(R.string.CARRY_ON));
                    bundle.putString(KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
                    bundle.putString(KEY_INPUT_HINT, getString(R.string.ENTER_PWD_1));
                    bundle.putInt(KEY_INPUT_LENGTH, 63);
                    bundle.putBoolean(KEY_EXCLUDE_CHINESE, true);
                    final int security = NetUtils.getSecurity(item);
                    bundle.putBoolean(KEY_SHOW_EDIT, security != 0);
                    if (info != null && !TextUtils.isEmpty(info.getPwd())) {
                        //填充密码
                        bundle.putString(KEY_DEFAULT_EDIT_TEXT, info.getPwd());
                    }
                    loadDialog(ssid, security, bundle);
                }, AppLogger::e);
    }

    private void loadDialog(final String ssid, final int security, Bundle bundle) {
        EditFragmentDialog dialog = EditFragmentDialog.newInstance(bundle);
        dialog.setAction((int id, Object value) -> {
            if (value != null && value instanceof String) {
                //pwd
                String routeName = NetUtils.getNetName(ContextUtils.getContext());
                Device device = DataSourceManager.getInstance().getDevice(uuid);
                DpMsgDefine.DPNet net = device == null ? new DpMsgDefine.DPNet() : device.$(201, new DpMsgDefine.DPNet());
                if (!TextUtils.equals(routeName, net.ssid)) {
                    ToastUtil.showNegativeToast(getString(R.string.setwifi_check, net.ssid));
                    return;
                }
                if (presenter != null) {
                    presenter.sendWifiInfo(uuid, ssid, (String) value, security);
                }
                ToastUtil.showToast(getString(R.string.DOOR_SET_WIFI_MSG));
                Intent intent = new Intent(this, NewHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onRefresh() {
        if (presenter != null) {
            presenter.refreshWifiList();
        }
        swRefreshWifi.setRefreshing(true);
    }

    @Override
    public boolean performBackIntercept(boolean willExit) {
        AlertDialogManager.getInstance().showDialog(this, getString(R.string.Tap1_AddDevice_tips), getString(R.string.Tap1_AddDevice_tips),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    finishExt();
                }, getString(R.string.CANCEL), null, false);
        return true;
    }

    private class AAdapter extends SuperAdapter<ScanResult> {

        private AAdapter(Context context, List<ScanResult> items, int layoutResId) {
            super(context, items, layoutResId);
        }

        @Override
        public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, ScanResult item) {
            String ssid = item.SSID.replace("\"", "");
            holder.setImageResource(R.id.imv_wifi_ssid, getWifiIcon(item));
            holder.setText(R.id.tv_wifi_ssid, ssid);
            Log.d("WifiList", "list: " + ssid);
        }

        private int getWifiIcon(ScanResult item) {
            int security = NetUtils.getSecurity(item);
            int strength = WifiManager.calculateSignalLevel(item.level, 3);
            if (security == 0) {
                //open
                strength += 3;
            } else {
                //encrypt
                strength += 0;
            }
            int base = R.drawable.setting_icon_wifi_network_security1;
            return base + strength;
        }
    }
}
