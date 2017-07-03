package com.cylan.jiafeigou.n.view.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.ConfigApPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanWifiList;
import com.cylan.jiafeigou.n.mvp.model.LocalWifiInfo;
import com.cylan.jiafeigou.n.view.bind.PanoramaExplainFragment;
import com.cylan.jiafeigou.n.view.bind.SubmitBindingInfoActivity;
import com.cylan.jiafeigou.n.view.bind.WiFiListDialogFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.LoginButton;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JConstant.JUST_SEND_INFO;
import static com.cylan.jiafeigou.misc.JConstant.KEY_BIND_DEVICE;
import static com.cylan.jiafeigou.utils.BindUtils.SECURITY_NONE;
import static com.cylan.jiafeigou.utils.BindUtils.SECURITY_WEP;

public class ConfigWifiActivity extends BaseBindActivity<ConfigApContract.Presenter>
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
    @BindView(R.id.iv_explain_gray)
    ImageView ivExplainGray;
    @BindView(R.id.lLayout_action)
    LinearLayout lLayoutAction;
    @BindView(R.id.img_complete)
    ImageView imgComplete;
    @BindView(R.id.img_upgrade)
    ImageView imgUpgrade;
    @BindView(R.id.upgrade_des)
    TextView upgradeDes;
    @BindView(R.id.update_btn)
    Button updateBtn;
    @BindView(R.id.lLayout_config_ap)
    RelativeLayout lLayoutConfigAp;

    private List<ScanResult> cacheList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_wifi);
        ButterKnife.bind(this);
        basePresenter = new ConfigApPresenterImpl(this);
        //默认隐藏
        ViewUtils.showPwd(etWifiPwd, false);
        ViewUtils.setViewMarginStatusBar(customToolbar);
        if (getIntent().hasExtra(JConstant.KEY_BIND_DEVICE_ALIAS)
                && TextUtils.equals(getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE_ALIAS),
                getString(R.string._720PanoramicCamera))) {
            ivExplainGray.setVisibility(View.VISIBLE);
            ViewUtils.setViewMarginStatusBar(ivExplainGray);
            ViewUtils.setViewMarginStatusBar(ivExplainGray);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (cacheList != null && cacheList.size() > 0) {
            tvConfigApName.setText(cacheList.get(0).SSID);
            tvConfigApName.setTag(new BeanWifiList(cacheList.get(0)));
        }
        customToolbar.setBackAction(v -> onBackPressed());
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
            LoadingDialog.showLoading(getSupportFragmentManager(),
                    getString(R.string.LOADING), false, null);
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

    @OnTextChanged(R.id.et_wifi_pwd)
    public void onPwdUpdate(CharSequence s, int start, int before, int count) {
        ivWifiClearPwd.setVisibility(TextUtils.isEmpty(s) ? View.INVISIBLE : View.VISIBLE);
    }

    @OnClick({R.id.iv_wifi_clear_pwd, R.id.tv_wifi_pwd_submit, R.id.tv_config_ap_name})
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        switch (view.getId()) {
            case R.id.iv_wifi_clear_pwd:
                etWifiPwd.setText("");
                LocalWifiInfo.Saver.getSaver().addOrUpdateInfo(new LocalWifiInfo()
                        .setSsid(tvConfigApName.getText().toString()).setPwd(""));
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
                if (type == SECURITY_NONE) {

                } else if (type == SECURITY_WEP) {
                    if (pwd.length() < 5) {
                        ToastUtil.showNegativeToast(getString(R.string.ENTER_PWD_1));
                        return;
                    }
                } else {
                    if (pwd.length() < 8) {
                        ToastUtil.showNegativeToast(getString(R.string.ENTER_PWD_1));
                        return;
                    }
                }
                tvWifiPwdSubmit.viewZoomSmall(null);
                //判断当前
                if (getIntent().hasExtra(JConstant.JUST_SEND_INFO)) {
                    if (basePresenter != null)
                        basePresenter.sendWifiInfo(getIntent().getStringExtra(JConstant.JUST_SEND_INFO),
                                ViewUtils.getTextViewContent(tvConfigApName),
                                ViewUtils.getTextViewContent(etWifiPwd), type);
                } else {
                    if (basePresenter != null)
                        basePresenter.sendWifiInfo(ViewUtils.getTextViewContent(tvConfigApName),
                                ViewUtils.getTextViewContent(etWifiPwd), type);
                }
                IMEUtils.hide(this);
                LocalWifiInfo.Saver.getSaver().addOrUpdateInfo(new LocalWifiInfo()
                        .setSsid(ssid).setPwd(pwd));
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
        AlertDialogManager.getInstance().showDialog(this, getString(R.string.Tap1_AddDevice_disconnected), getString(R.string.Tap1_AddDevice_disconnected),
                getString(R.string.OK), (dialog, which) -> {
                    basePresenter.finish();
                    final String className = getIntent().getStringExtra(JConstant.KEY_COMPONENT_NAME);
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(this, className));
                    startActivity(intent);
                }, false);
    }


    @Override
    public void onBackPressed() {
        IMEUtils.hide(this);
        AlertDialogManager.getInstance().showDialog(this, getString(R.string.Tap1_AddDevice_tips), getString(R.string.Tap1_AddDevice_tips),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    if (getIntent() != null && getIntent().hasExtra(JConstant.JUST_SEND_INFO)) {
                        finishExt();
                    } else {
                        if (getIntent().getStringExtra("PanoramaConfigure") != null) {
                            finishExt();
                        } else {
                            Intent intent = new Intent(ConfigWifiActivity.this, BindDeviceActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }

                }, getString(R.string.CANCEL), null, false);
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
        if (state != ConnectivityManager.TYPE_WIFI) {
        }
//            ToastUtil.showNegativeToast(getString(R.string.NoNetworkTips));
        else {
            AlertDialogManager.getInstance().dismissOtherDialog(getString(R.string.Tap1_AddDevice_disconnected));
        }
    }

    @Override
    public void onWiFiResult(List<ScanResult> resultList) {
        final int count = resultList == null ? 0 : resultList.size();
        if (count == 0) {
            if (Build.VERSION.SDK_INT >= 23) {
//                ToastUtil.showNegativeToast(getString(R.string.GetWifiList_FaiTips));
            }
            return;
        }
        cacheList = resultList;
        if (fiListDialogFragment != null)
            fiListDialogFragment.updateList(cacheList, tvConfigApName.getTag());
        Object object = tvConfigApName.getTag();
        etWifiPwd.getText().clear();
        if (object == null) {
            getInterestingSSid(resultList);
//            tvConfigApName.setTag(new BeanWifiList(resultList.get(0)));
//            tvConfigApName.setText(resultList.get(0).SSID);
//            LocalWifiInfo.Saver.getSaver().getInfo(resultList.get(0).SSID.replace("\"", ""))
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(info -> {
//                        if (info != null && !TextUtils.isEmpty(info.getPwd())) {
//                            //填充密码
//                            etWifiPwd.setText(info.getPwd());
//                        } else etWifiPwd.setText("");
//                    }, AppLogger::e);
        }
    }

    private void getInterestingSSid(List<ScanResult> resultList) {
        if (!ListUtils.isEmpty(resultList))
            LocalWifiInfo.Saver.getSaver().getMap()
                    .subscribeOn(Schedulers.newThread())
                    .flatMap(map -> {
                        if (map != null)
                            for (ScanResult result : resultList) {
                                final String ssid = result.SSID.replace("\"", "");
                                if (!map.containsKey(ssid)) continue;
                                LocalWifiInfo info = map.get(ssid);
                                if (info != null && !TextUtils.isEmpty(info.getPwd())) {
                                    return Observable.just(new Pair<>(info, new BeanWifiList(result)));
                                }
                            }
                        if (ListUtils.isEmpty(resultList))
                            return Observable.just(null);
                        else
                            return Observable.just(new Pair<>(new LocalWifiInfo(), new BeanWifiList(resultList.get(0))));
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {

                        if (ret != null) {
                            LocalWifiInfo info = ret.first;
                            if (info != null && !TextUtils.isEmpty(info.getPwd()) && !TextUtils.isEmpty(info.getSsid())) {
                                tvConfigApName.setText(resultList.get(0).SSID);
                                etWifiPwd.setText(info.getPwd());
                            }
                            if (ret.second != null) {
                                tvConfigApName.setTag(ret.second);
                                tvConfigApName.setText(ret.second.result.SSID.replace("\"", ""));
                            }
                        }
                    }, AppLogger::e);
    }

    @Override
    public void onSetWifiFinished(UdpConstant.UdpDevicePortrait o) {
        LoadingDialog.dismissLoading(getSupportFragmentManager());
        if (getIntent().hasExtra(JUST_SEND_INFO)) {
            runOnUiThread(() -> ToastUtil.showPositiveToast(getString(R.string.DOOR_SET_WIFI_MSG)));
            Intent intent = new Intent(this, NewHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Intent intent = getIntent();// new Intent(this, SubmitBindingInfoActivity.class);
            intent.setClass(this, SubmitBindingInfoActivity.class);
            intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, o.uuid);
            intent.putExtra(KEY_BIND_DEVICE, getIntent().getStringExtra(KEY_BIND_DEVICE));
            startActivity(intent);
            finishExt();
        }
        if (basePresenter != null) {
            basePresenter.finish();
        }
    }

    @Override
    public void sendWifiInfoFailed() {
        runOnUiThread(() -> {
            LoadingDialog.dismissLoading(getSupportFragmentManager());
            tvWifiPwdSubmit.viewZoomBig();
        });
    }

    @Override
    public void check3gFinish() {
        LoadingDialog.dismissLoading(getSupportFragmentManager());
    }


    @Override
    public void upgradeDogState(int state) {
        if (vsShowContent.getCurrentView().getId() == R.id.fragment_config_ap_pre) {
            vsShowContent.showNext();
        }
    }

    @Override
    public void pingFailed() {
//        ToastUtil.showNegativeToast(getString(R.string.ADD_FAILED));
        tvWifiPwdSubmit.viewZoomBig();
    }

    @Override
    public void onDeviceAlreadyExist() {
        tvWifiPwdSubmit.cancelAnim();
        tvWifiPwdSubmit.viewZoomBig();
        tvWifiPwdSubmit.setText(R.string.NEXT);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.RET_EISBIND_BYSELF)
                .setMessage(R.string.RET_ESCENE_DELETE_EXIST_CID)
                .setCancelable(false)
                .setPositiveButton(R.string.TRY_AGAIN, (dialog, which) -> {
                    final String className = getIntent().getStringExtra(JConstant.KEY_COMPONENT_NAME);
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(this, className));
                    startActivity(intent);
                })
                .show();
    }


    @Override
    public void onDismiss(ScanResult scanResult) {
        tvConfigApName.setTag(new BeanWifiList(scanResult));
        tvConfigApName.setText(scanResult.SSID);
        rLayoutWifiPwdInputBox.setVisibility(BindUtils.getSecurity(scanResult) != 0
                ? View.VISIBLE : View.GONE);
        LocalWifiInfo.Saver.getSaver().getInfo(scanResult.SSID.replace("\"", ""))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(info -> {
                    if (info != null && !TextUtils.isEmpty(info.getPwd())) {
                        //填充密码
                        etWifiPwd.setText(info.getPwd());
                    } else etWifiPwd.setText("");
                }, AppLogger::e);
    }

    @OnClick(R.id.lLayout_config_ap)
    public void onClick() {
        IMEUtils.hide(this);
    }

    @OnClick(R.id.iv_explain_gray)
    public void onExplain() {
        PanoramaExplainFragment fragment = PanoramaExplainFragment.newInstance(null);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                fragment, android.R.id.content);
    }
}
