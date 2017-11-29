package com.cylan.jiafeigou.n.view.bind;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.SubmitBindingInfoImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoginButton;
import com.cylan.jiafeigou.widget.SimpleProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SubmitBindingInfoActivity extends BaseFullScreenFragmentActivity<SubmitBindingInfoContract.Presenter>
        implements SubmitBindingInfoContract.View {
    @BindView(R.id.progress_loading)
    SimpleProgressBar progressLoading;
    @BindView(R.id.tv_loading_percent)
    TextView tvLoadingPercent;
    @BindView(R.id.btn_bind_failed_repeat)
    LoginButton btnBindRepeat;
    @BindView(R.id.vs_layout_switch)
    ViewSwitcher vsLayoutSwitch;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.iv_explain_gray)
    ImageView ivExplainGray;
    @BindView(R.id.submit_description)
    TextView submitDescription;
    @BindView(R.id.tv_bind_failed_text)
    TextView tvBindFailedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_submit_binding_info);
        ButterKnife.bind(this);
        this.presenter = new SubmitBindingInfoImpl(this, getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID));
        adjustViewSize();
        customToolbar.setBackAction(v -> {
            onBackPressed();
        });
        if (getIntent().hasExtra(JConstant.KEY_BIND_DEVICE_ALIAS)
                && TextUtils.equals(getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE_ALIAS),
                getString(R.string._720PanoramicCamera))) {
            ViewUtils.setViewMarginStatusBar(ivExplainGray);
        }
        boolean just_config = getIntent().getBooleanExtra("just_config", false);
        submitDescription.setText(just_config ? R.string.CONNECTING_1 : R.string.DEVICE_ADDING);
        presenter.sendBindRequest();
    }

    private void adjustViewSize() {
        ViewGroup.LayoutParams l = progressLoading.getLayoutParams();
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        l.height = l.width = (int) (screenWidth * 0.6f);
        progressLoading.setLayoutParams(l);
    }

    @Override
    public boolean performBackIntercept(boolean willExit) {
        AlertDialogManager.getInstance().showDialog(this, getString(R.string.Tap1_AddDevice_tips), getString(R.string.Tap1_AddDevice_tips),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    onBindNext();
                }, getString(R.string.CANCEL), null, false);
        return true;
    }

    @Override
    public void onCounting(int percent) {
        Log.d("SubmitBindingInfo", "SubmitBindingInfo: " + percent);
        tvLoadingPercent.post(() -> tvLoadingPercent.setText(percent + "%"));//现在需要显示百分比
    }

    @Override
    public void onBindSuccess() {
        AppLogger.d("onBindSuccess");

        progressLoading.setVisibility(View.INVISIBLE);
        if (presenter != null) {
            presenter.unsubscribe();
        }

        String panoramaConfigure = getIntent().getStringExtra("PanoramaConfigure");
        AppLogger.d("panoramaConfigure:" + panoramaConfigure);
        if (TextUtils.isEmpty(panoramaConfigure)) {
            Bundle bundle = new Bundle();
            bundle.putString(JConstant.KEY_BIND_DEVICE_ALIAS, getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE));
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID));
            SetDeviceAliasFragment fragment = SetDeviceAliasFragment.newInstance(bundle);
            ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                    fragment, android.R.id.content);
        } else {
            Bundle bundle = new Bundle();
            customToolbar.setVisibility(View.INVISIBLE);
            bundle.putString("PanoramaConfigure", panoramaConfigure);
            bundle.putBoolean("Success", true);
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID));
            ConfigPanoramaWiFiSuccessFragment newInstance = ConfigPanoramaWiFiSuccessFragment.newInstance(bundle);
            ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                    newInstance, android.R.id.content);
        }

    }

    @Override
    public void onBindFailed() {
        AppLogger.d("onBindFailed");
        vsLayoutSwitch.showNext();
        if (getIntent().hasExtra(JConstant.KEY_BIND_DEVICE_ALIAS)
                && TextUtils.equals(getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE_ALIAS),
                getString(R.string._720PanoramicCamera))) {
            ivExplainGray.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBindTimeout() {
        AppLogger.d("onBindTimeout");
        AppLogger.d("绑定失败了!!!!!!!!!!!!!");
        progressLoading.setVisibility(View.INVISIBLE);
        vsLayoutSwitch.showNext();
        if (getIntent().hasExtra(JConstant.KEY_BIND_DEVICE_ALIAS)
                && TextUtils.equals(getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE_ALIAS),
                getString(R.string._720PanoramicCamera))) {
            ivExplainGray.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRebindRequired(UdpConstant.UdpDevicePortrait portrait, String reason) {
        AppLogger.d("onRebindRequired,reason:" + reason + ",portrait:" + portrait);
        switch (portrait.pid) {
            case 84: {// TODO: 2017/11/28 DC 11 不支持强绑 ,DC11没有 WiFi 模块
                AppLogger.d("设备已被其他用户绑定:" + reason);
                progressLoading.setVisibility(View.INVISIBLE);
                vsLayoutSwitch.setDisplayedChild(1);
                tvBindFailedText.setText(getString(R.string.Tap3_UserMessage_DeviceUnbind, reason));
                if (getIntent().hasExtra(JConstant.KEY_BIND_DEVICE_ALIAS)
                        && TextUtils.equals(getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE_ALIAS), getString(R.string._720PanoramicCamera))) {
                    ivExplainGray.setVisibility(View.VISIBLE);
                }
            }
            break;
            default: {
                getAlertDialogManager().showDialog(this, "reBind", getString(R.string.DEVICE_EXISTED),
                        getString(R.string.OK), (DialogInterface dialog, int which) -> {
                            //强绑提示按钮,
                            onBindNext();
                        }, false);
            }
            break;
        }

    }

    @Override
    public void onBindCidNotExist() {
        AppLogger.d("onBindCidNotExist");
        getAlertDialogManager().showDialog(this, "null", getString(R.string.RET_ECID_INVALID),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    onBindNext();
                }, getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                }, false);
        if (presenter != null) {
            presenter.unsubscribe();
        }
    }

    @OnClick(R.id.btn_bind_failed_repeat)
    public void onBindNext() {
        presenter.unsubscribe();
        final String className = getIntent().getStringExtra(JConstant.KEY_BIND_BACK_ACTIVITY);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, className));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishExt();
    }

    @OnClick(R.id.iv_explain_gray)
    public void onExplain() {
        PanoramaExplainFragment fragment = PanoramaExplainFragment.newInstance(null);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                fragment, android.R.id.content);
    }
}
