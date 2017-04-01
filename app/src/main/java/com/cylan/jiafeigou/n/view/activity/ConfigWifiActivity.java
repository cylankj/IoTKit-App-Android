package com.cylan.jiafeigou.n.view.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.ConfigApPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanWifiList;
import com.cylan.jiafeigou.n.view.bind.SubmitBindingInfoFragment;
import com.cylan.jiafeigou.n.view.bind.WiFiListDialogFragment;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoginButton;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.cylan.jiafeigou.misc.JConstant.JUST_SEND_INFO;

@RuntimePermissions
public class ConfigWifiActivity extends BaseFullScreenFragmentActivity<ConfigApContract.Presenter>
        implements ConfigApContract.View, WiFiListDialogFragment.ClickCallBack {
    @BindView(R.id.iv_wifi_clear_pwd)
    ImageView ivWifiClearPwd;
    @BindView(R.id.cb_wifi_pwd)
    CheckBox cbWifiPwd;
    @BindView(R.id.tv_wifi_pwd_submit)
    LoginButton tvWifiPwdSubmit;
    @BindView(R.id.et_wifi_pwd)
    EditText etWifiPwd;
    @BindView(R.id.tv_config_ap_name)
    TextView tvConfigApName;

    WiFiListDialogFragment fiListDialogFragment;
    @BindView(R.id.rLayout_wifi_pwd_input_box)
    RelativeLayout rLayoutWifiPwdInputBox;
    @BindView(R.id.vs_show_content)
    ViewSwitcher vsShowContent;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    private AlertDialog reBingDialog;

    private List<ScanResult> cacheList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_wifi);
        ButterKnife.bind(this);
        basePresenter = new ConfigApPresenterImpl(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        ConfigWifiActivityPermissionsDispatcher.onGrantedLocationPermissionWithCheck(this);
        if (cacheList != null && cacheList.size() > 0) {
            tvConfigApName.setText(cacheList.get(0).SSID);
            tvConfigApName.setTag(new BeanWifiList(cacheList.get(0)));
        }
        //默认显示
        ViewUtils.showPwd(etWifiPwd, true);
        customToolbar.setBackAction(v -> {
            onBackPressed();
        });
        cbWifiPwd.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            ViewUtils.showPwd(etWifiPwd, isChecked);
            etWifiPwd.setSelection(etWifiPwd.length());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initFragment();
        if (basePresenter != null) {
            basePresenter.refreshWifiList();
            basePresenter.check3GDogCase();
        }
    }

    private void initFragment() {
        if (fragmentWeakReference == null || fragmentWeakReference.get() == null)
            fragmentWeakReference = new WeakReference<>(WiFiListDialogFragment.newInstance(new Bundle()));
    }

    private WeakReference<WiFiListDialogFragment> fragmentWeakReference;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fragmentWeakReference != null
                && fragmentWeakReference.get() != null
                && fragmentWeakReference.get().isResumed()) {
            fragmentWeakReference.get().dismiss();
        }
    }

    @OnClick({R.id.iv_wifi_clear_pwd, R.id.tv_wifi_pwd_submit, R.id.tv_config_ap_name})
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        switch (view.getId()) {
            case R.id.iv_wifi_clear_pwd:
                etWifiPwd.setText("");
                break;
            case R.id.tv_wifi_pwd_submit:
                int currentNet = NetUtils.getJfgNetType(getApplicationContext());
                if (currentNet != ConnectivityManager.TYPE_WIFI) {
                    createDialog();
                    return;
                }
                String ssid = tvConfigApName.getText().toString();
                String pwd = ViewUtils.getTextViewContent(etWifiPwd);
                int type = 0;
                if (TextUtils.isEmpty(ssid)) {
//                    ToastUtil.showNegativeToast("没有文案:请选择wifi");
                    return;
                }
                Object o = tvConfigApName.getTag();
                if (o != null && o instanceof BeanWifiList) {
                    type = BindUtils.getSecurity(((BeanWifiList) o).result);
                }
                if (type != 0 && pwd.length() < 8) {
                    ToastUtil.showNegativeToast(getString(R.string.ENTER_PWD_1));
                    return;
                }
                if (basePresenter != null)
                    basePresenter.sendWifiInfo(ViewUtils.getTextViewContent(tvConfigApName),
                            ViewUtils.getTextViewContent(etWifiPwd), type);
                tvWifiPwdSubmit.viewZoomSmall();
                if (getIntent().hasExtra(JUST_SEND_INFO)) {
                    ToastUtil.showPositiveToast(getString(R.string.DOOR_SET_WIFI_MSG));
                    Intent intent = new Intent(this, NewHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return;
                }
                break;
            case R.id.tv_config_ap_name:
                initFragment();
                fiListDialogFragment = fragmentWeakReference.get();
                fiListDialogFragment.setClickCallBack(this);
                fiListDialogFragment.updateList(cacheList, tvConfigApName.getTag());
                fiListDialogFragment.show(getSupportFragmentManager(), "WiFiListDialogFragment");
                if (basePresenter != null) {
                    basePresenter.refreshWifiList();
                }
                break;
        }
    }

    private void createDialog() {
        if (reBingDialog == null) {
            reBingDialog = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.Tap1_AddDevice_disconnected))
                    .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    })
                    .setPositiveButton(getString(R.string.OK), null)
                    .create();
        }
        if (reBingDialog.isShowing()) return;
        reBingDialog.show();
    }

    private AlertDialog backDialog;

    @Override
    public void onBackPressed() {
        if (backDialog != null && backDialog.isShowing()) return;
        if (backDialog == null) backDialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.Tap1_AddDevice_tips))
                .setNegativeButton(getString(R.string.CANCEL), null)
                .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    finishExt();
                })
                .setCancelable(false)
                .create();
        backDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ConfigWifiActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        if (permissions.length == 1) {
            if (TextUtils.equals(permissions[0], ACCESS_FINE_LOCATION) && grantResults[0] > -1) {
                ConfigWifiActivityPermissionsDispatcher.onGrantedLocationPermissionWithCheck(this);
            }
        }
    }


    @NeedsPermission(ACCESS_FINE_LOCATION)
    public void onGrantedLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (!MiscUtils.checkGpsAvailable(getApplicationContext())) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.GetWifiList_FaiTips))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.OK), (@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) -> {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        })
                        .setNegativeButton(getString(R.string.CANCEL), (final DialogInterface dialog, @SuppressWarnings("unused") final int id) -> {
                            dialog.cancel();
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
    }

    @OnPermissionDenied(ACCESS_FINE_LOCATION)
    public void onDeniedLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            new AlertDialog.Builder(this)
                    .setMessage(String.format(getString(R.string.turn_on_gps), ""))
                    .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    })
                    .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                    })
                    .create()
                    .show();
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    public void showRationaleForLocation(PermissionRequest request) {
        onDeniedLocationPermission();
    }


    @Override
    public void setPresenter(ConfigApContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void onNetStateChanged(int state) {
        if (state != ConnectivityManager.TYPE_WIFI)
            ToastUtil.showNegativeToast(getString(R.string.NoNetworkTips));
        else {
            dismissDialog();
        }
    }

    private void dismissDialog() {
        if (reBingDialog != null && reBingDialog.isShowing()) reBingDialog.dismiss();
    }

    @Override
    public void onWiFiResult(List<ScanResult> resultList) {
        final int count = resultList == null ? 0 : resultList.size();
        if (count == 0) {
            if (Build.VERSION.SDK_INT >= 23) {
                ToastUtil.showNegativeToast(getString(R.string.GetWifiList_FaiTips));
            }
            return;
        }
        cacheList = resultList;
        if (fiListDialogFragment != null)
            fiListDialogFragment.updateList(cacheList, tvConfigApName.getTag());
        Object object = tvConfigApName.getTag();
        if (object == null) {
            tvConfigApName.setTag(new BeanWifiList(resultList.get(0)));
            tvConfigApName.setText(resultList.get(0).SSID);
        }
    }

    @Override
    public void onSetWifiFinished(UdpConstant.UdpDevicePortrait o) {
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_BIND_DEVICE, getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE));
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, o.uuid);
        SubmitBindingInfoFragment fragment = SubmitBindingInfoFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                fragment, android.R.id.content);
        if (basePresenter != null) {
            basePresenter.finish();
        }
    }

    @Override
    public void lossDogConnection() {
    }


    @Override
    public void upgradeDogState(int state) {
        if (vsShowContent.getCurrentView().getId() == R.id.fragment_config_ap_pre) {
            vsShowContent.showNext();
        }
    }

    @Override
    public void pingFailed() {
        ToastUtil.showNegativeToast(getString(R.string.ADD_FAILED));
        tvWifiPwdSubmit.viewZoomBig();
    }


    @Override
    public void onDismiss(ScanResult scanResult) {
        tvConfigApName.setTag(new BeanWifiList(scanResult));
        tvConfigApName.setText(scanResult.SSID);
        rLayoutWifiPwdInputBox.setVisibility(BindUtils.getSecurity(scanResult) != 0
                ? View.VISIBLE : View.GONE);
    }
}
