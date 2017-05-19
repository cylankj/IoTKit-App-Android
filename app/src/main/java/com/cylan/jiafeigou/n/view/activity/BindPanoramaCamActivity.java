package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.ApFilter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.bind.BindGuideActivity;
import com.cylan.jiafeigou.n.view.bind.ConfigPanoramaWiFiSuccessFragment;
import com.cylan.jiafeigou.n.view.bind.PanoramaExplainFragment;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.nineoldandroids.animation.Animator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class BindPanoramaCamActivity extends BaseBindActivity {
    @BindView(R.id.imgV_camera_wifi_light_flash)
    ImageView imgVCameraWifiLightFlash;
    @BindView(R.id.imgV_camera_hand)
    ImageView imgVCameraHand;
    @BindView(R.id.imgV_camera_red_dot)
    ImageView imgVCameraRedDot;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.iv_explain_gray)
    ImageView expainGray;
    private Animator animator;
    @BindView(R.id.imgv_camera_wifi_light_flash_bg)
    View bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String panoramaConfigure = getIntent().getStringExtra("PanoramaConfigure");
        final WifiInfo info = NetUtils.getWifiManager(ContextUtils.getContext()).getConnectionInfo();
        if (TextUtils.equals(panoramaConfigure, "OutDoor") && info != null && ApFilter.isAPMode(info.getSSID(), getUuid())
                && NetUtils.getNetType(ContextUtils.getContext()) == ConnectivityManager.TYPE_WIFI) {
            Bundle bundle = new Bundle();
            bundle.putString("PanoramaConfigure", panoramaConfigure);
            bundle.putBoolean("Success", true);
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID));
            ConfigPanoramaWiFiSuccessFragment newInstance = ConfigPanoramaWiFiSuccessFragment.newInstance(bundle);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    newInstance, android.R.id.content);
        } else {
            setContentView(R.layout.activity_bind_panorama_cam);
            ButterKnife.bind(this);
            customToolbar.setBackAction(v -> finishExt());
            initAnimation();
            ViewUtils.setViewMarginStatusBar(customToolbar);
            ViewUtils.setViewMarginStatusBar(expainGray);
        }
    }

    private void initAnimation() {
        animator = AnimatorUtils.onHandMoveAndFlashPanorama(imgVCameraHand, imgVCameraRedDot, imgVCameraWifiLightFlash, bg);
        animator.start();
    }

    @OnClick(R.id.tv_bind_camera_tip)
    public void onClick() {
        Intent intent = getIntent();
        intent.setClass(this, BindGuideActivity.class);
        intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.DOG_CAMERA_NAME));
        intent.putExtra(JConstant.KEY_BIND_DEVICE_ALIAS, getString(R.string.DOG_CAMERA_NAME));
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.iv_explain_gray)
    public void onExplain() {
        PanoramaExplainFragment fragment = PanoramaExplainFragment.newInstance(null);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                fragment, android.R.id.content);
    }

    private void cancelAnimation() {
        if (animator != null && animator.isRunning())
            animator.cancel();
    }
}
