package com.cylan.jiafeigou.n.view.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.n.mvp.contract.bind.Config4GContract;
import com.cylan.jiafeigou.n.view.bind.SubmitBindingInfoActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.APObserver;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.JUST_SEND_INFO;
import static com.cylan.jiafeigou.misc.JConstant.KEY_BIND_DEVICE;

/**
 * Created by yanzhendong on 2018/3/25.
 */

public class Config4GActivity extends BaseActivity<Config4GContract.Presenter> implements Config4GContract.View {
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.imv_gif_container)
    ImageView imgvGifContainer;

    @Override
    protected boolean onSetContentView() {
        setContentView(R.layout.activity_config_4g);
        return true;
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        customToolbar.setBackAction(this::onBackButtonClicked);
        GlideApp.with(this)
                .load(R.raw.cam_sim)
                .into(imgvGifContainer);
    }

    private void onBackButtonClicked(View view) {
        AlertDialogManager.getInstance().showDialog(this, getString(R.string.Tap1_AddDevice_tips), getString(R.string.Tap1_AddDevice_tips),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    final String className = getIntent().getStringExtra(JConstant.KEY_BIND_BACK_ACTIVITY);
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(this, className));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }, getString(R.string.CANCEL), null, false);
    }

    @OnClick(R.id.tv_next_step)
    void onNextStepClicked() {
        presenter.performSIMCheckerAndGoNext();
    }

    @Override
    public void onSIMCheckerFailed(APObserver.ScanResult scanResult) {
        AppLogger.e("onSIMCheckerFailed");
        ToastUtil.showToast(ContextUtils.getContext().getString(R.string.CAMERA4G_NOSIM));
    }

    @Override
    public void onSIMCheckerSuccess(APObserver.ScanResult scanResult) {
        if (getIntent().hasExtra(JUST_SEND_INFO) && !getIntent().getBooleanExtra("just_config", false)) {
            runOnUiThread(() -> ToastUtil.showPositiveToast(getString(R.string.DOOR_SET_WIFI_MSG)));
            Intent intent = new Intent(this, NewHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Intent intent = getIntent();// new Intent(this, SubmitBindingInfoActivity.class);
            intent.setClass(this, SubmitBindingInfoActivity.class);
            intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, scanResult.getUuid());
            intent.putExtra(KEY_BIND_DEVICE, getIntent().getStringExtra(KEY_BIND_DEVICE));
            startActivity(intent);
        }
    }

    @Override
    public void onApnUpdateRequired(APObserver.ScanResult scanResult) {
        AppLogger.d("Config4GActivity,onApnUpdateRequired");
    }

    @Override
    public void onApnUpdateError() {
        AppLogger.d("Config4GActivity,onApnUpdateError");
    }

    @Override
    public void onApnUpdateTimeout() {
        AppLogger.d("Config4GActivity,onApnUpdateTimeout");
    }

    @Override
    public void onApnUpdateSuccess() {
        AppLogger.d("Config4GActivity,onApnUpdateSuccess");
    }
}
