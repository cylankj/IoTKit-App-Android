package com.cylan.jiafeigou.n.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.ApFilter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.bind.BindGuideActivity;
import com.cylan.jiafeigou.n.view.bind.ConfigPanoramaWiFiSuccessFragment;
import com.cylan.jiafeigou.n.view.bind.PanoramaExplainFragment;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

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
    @BindView(R.id.imgv_camera_wifi_light_flash_bg)
    View bg;
    @BindView(R.id.tv_main_content)
    TextView tvMainContent;
    @BindView(R.id.tv_description)
    TextView tvDescription;
    @BindView(R.id.tv_bind_camera_tip)
    TextView tvBindCameraTip;
    private AnimatorSet animator;

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
            tvMainContent.setText(getIntent().getStringExtra(JConstant.KEY_ANIM_TITLE));
            tvDescription.setText(getIntent().getStringExtra(JConstant.KEY_ANIM_SUB_TITLE));
            tvBindCameraTip.setText(getIntent().getStringExtra(JConstant.KEY_NEXT_STEP));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (expainGray != null)
            ViewUtils.setViewMarginStatusBar(expainGray);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (expainGray != null) {
            ViewUtils.clearViewMarginStatusBar(expainGray);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    private void initAnimation() {
        if (animator == null) {
            animator = new AnimatorSet();
            imgVCameraHand.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            ObjectAnimator hand = ObjectAnimator.ofFloat(imgVCameraHand, "translationX", imgVCameraHand.getMeasuredWidth(), 0);
            hand.setDuration(600);
            hand.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    bg.setVisibility(View.VISIBLE);
                }
            });

            AnimatorSet flash = new AnimatorSet();
            ObjectAnimator redDot = ObjectAnimator.ofFloat(imgVCameraRedDot, "alpha", 0f, 1f, 0f);
            redDot.setRepeatCount(3);
            redDot.setRepeatMode(ValueAnimator.RESTART);
            ObjectAnimator lightFlash = ObjectAnimator.ofFloat(imgVCameraWifiLightFlash, "alpha", 0f, 1f, 0f);
            lightFlash.setRepeatCount(3);
            lightFlash.setRepeatMode(ValueAnimator.RESTART);
            flash.playTogether(redDot, lightFlash);
            flash.setDuration(600);
            animator.playSequentially(hand, flash);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    bg.setVisibility(View.INVISIBLE);
                    animation.start();
                }
            });
            animator.start();
        }
    }

    @OnClick(R.id.tv_bind_camera_tip)
    public void onClick() {
        Intent intent = getIntent();
        intent.setClass(this, BindGuideActivity.class);
        intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string._720PanoramicCamera));
        intent.putExtra(JConstant.KEY_BIND_DEVICE_ALIAS, getString(R.string._720PanoramicCamera));
        intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bind_guide);
        intent.putExtra(JConstant.KEY_SSID_PREFIX, BindUtils.DOG_AP);
        intent.putExtra(JConstant.KEY_COMPONENT_NAME, this.getClass().getName());
        startActivity(intent);

    }

    @OnClick(R.id.iv_explain_gray)
    public void onExplain() {
        PanoramaExplainFragment fragment = PanoramaExplainFragment.newInstance(null);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                fragment, android.R.id.content);
    }
}
