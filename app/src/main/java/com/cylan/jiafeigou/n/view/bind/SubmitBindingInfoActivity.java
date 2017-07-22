package com.cylan.jiafeigou.n.view.bind;

import android.content.ComponentName;
import android.content.Context;
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
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.SubmitBindingInfoImpl;
import com.cylan.jiafeigou.n.view.activity.BindDeviceActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.BindUtils;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_submit_binding_info);
        ButterKnife.bind(this);
        this.basePresenter = new SubmitBindingInfoImpl(this, getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID));
        adjustViewSize();
        customToolbar.setBackAction(v -> {
//            onBackPressed();
        });
        if (getIntent().hasExtra(JConstant.KEY_BIND_DEVICE_ALIAS)
                && TextUtils.equals(getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE_ALIAS),
                getString(R.string._720PanoramicCamera))) {
            ViewUtils.setViewMarginStatusBar(ivExplainGray);
            ViewUtils.setViewMarginStatusBar(ivExplainGray);
        }
    }

    private void adjustViewSize() {
        ViewGroup.LayoutParams l = progressLoading.getLayoutParams();
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        l.height = l.width = (int) (screenWidth * 0.6f);
        progressLoading.setLayoutParams(l);
    }

    @Override
    public void setPresenter(SubmitBindingInfoContract.Presenter presenter) {

    }


    @Override
    public void onBackPressed() {
        AlertDialogManager.getInstance().showDialog(this, getString(R.string.Tap1_AddDevice_tips), getString(R.string.Tap1_AddDevice_tips),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    onBindNext();
                }, getString(R.string.CANCEL), null, false);
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void bindState(int state) {
        runOnUiThread(() -> {
            if (state == BindUtils.BIND_FAILED) {//失败
                vsLayoutSwitch.showNext();
                ivExplainGray.setVisibility(View.VISIBLE);
//            customToolbar.setVisibility(View.INVISIBLE);
            } else if (state == JError.ErrorCIDBinded) {//强绑
                getAlertDialogManager().showDialog(this, "reBind", getString(R.string.DEVICE_EXISTED),
                        getString(R.string.OK), (DialogInterface dialog, int which) -> {
                            //强绑提示按钮,
                            onBindNext();
                        }, false);
            } else if (state == BindUtils.BIND_SUC) {//成功
                progressLoading.setVisibility(View.INVISIBLE);
                if (basePresenter != null)
                    basePresenter.stop();
                String panoramaConfigure = getIntent().getStringExtra("PanoramaConfigure");
                AppLogger.e("AAAAAAAAAA" + panoramaConfigure);
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

            } else if (state == BindUtils.BIND_NULL) {
                getAlertDialogManager().showDialog(this, "null", getString(R.string.RET_ECID_INVALID),
                        getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        }, getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                        }, false);
                if (basePresenter != null)
                    basePresenter.stop();
            } else {
                AppLogger.d("绑定失败了!!!!!!!!!!!!!");
                progressLoading.setVisibility(View.INVISIBLE);
                vsLayoutSwitch.showNext();
                ivExplainGray.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onCounting(int percent) {
        Log.d("SubmitBindingInfo", "SubmitBindingInfo: " + percent);
        tvLoadingPercent.post(() -> tvLoadingPercent.setText(percent + "%"));//现在需要显示百分比
    }

    @OnClick(R.id.btn_bind_failed_repeat)
    public void onBindNext() {
        final String className = getIntent().getStringExtra(JConstant.KEY_COMPONENT_NAME);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, BindDeviceActivity.class));
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
