package com.cylan.jiafeigou.n.view.bind;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.SubmitBindingInfoContractImpl;
import com.cylan.jiafeigou.n.view.activity.BindBellActivity;
import com.cylan.jiafeigou.n.view.activity.BindCamActivity;
import com.cylan.jiafeigou.n.view.activity.BindPanoramaCamActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.BindUtils;
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


    //    private AlertDialog needRebindDialog;
    private AlertDialog nullCidDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_submit_binding_info);
        ButterKnife.bind(this);
        this.basePresenter = new SubmitBindingInfoContractImpl(this, getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID));
        adjustViewSize();
        customToolbar.setBackAction(v -> {
            onBackPressed();
        });
        if (basePresenter != null) {
            basePresenter.startCounting();
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

    private android.app.AlertDialog backDialog;

    @Override
    public void onBackPressed() {
        if (backDialog != null && backDialog.isShowing()) return;
        if (backDialog == null) backDialog = new android.app.AlertDialog.Builder(this)
                .setMessage(getString(R.string.Tap1_AddDevice_tips))
                .setNegativeButton(getString(R.string.CANCEL), null)
                .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    onClick();
                })
                .setCancelable(false)
                .create();
        backDialog.show();
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void bindState(int state) {
        if (state == BindUtils.BIND_FAILED) {//失败
            //绑定失败
            vsLayoutSwitch.showNext();
//            customToolbar.setVisibility(View.INVISIBLE);
        } else if (state == BindUtils.BIND_NEED_REBIND) {//强绑
            basePresenter.endCounting();
        } else if (state == BindUtils.BIND_SUC) {//成功
            progressLoading.setVisibility(View.INVISIBLE);
            if (basePresenter != null)
                basePresenter.stop();
            Bundle bundle = new Bundle();
            bundle.putString(JConstant.KEY_BIND_DEVICE_ALIAS, getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE));
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID));
            SetDeviceAliasFragment fragment = SetDeviceAliasFragment.newInstance(bundle);
            ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                    fragment, android.R.id.content);
            if (basePresenter != null)
                basePresenter.stop();
        } else if (state == BindUtils.BIND_NULL) {
            if (nullCidDialog != null && nullCidDialog.isShowing()) return;
            nullCidDialog = new AlertDialog.Builder(this)
                    .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    })
                    .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    })
                    .create();
            nullCidDialog.show();
            if (basePresenter != null)
                basePresenter.stop();
        } else {
            AppLogger.d("绑定失败了!!!!!!!!!!!!!");
            if (vsLayoutSwitch.getDisplayedChild() == 0) {
                vsLayoutSwitch.showNext();
//                customToolbar.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onCounting(int percent) {
        Log.d("SubmitBindingInfo", "SubmitBindingInfo: " + percent);
        tvLoadingPercent.setText(percent + "");
    }

    @OnClick(R.id.btn_bind_failed_repeat)
    public void onClick() {
        String device = getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE);
        Class<?> nextActivity;
        if (TextUtils.equals(device, getString(R.string.DOG_CAMERA_NAME))) {
            nextActivity = BindCamActivity.class;
        } else if (TextUtils.equals(device, getString(R.string.CALL_CAMERA_NAME))) {
            nextActivity = BindBellActivity.class;
        } else {
            nextActivity = BindPanoramaCamActivity.class;
        }
        Intent intent = new Intent(this, nextActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
