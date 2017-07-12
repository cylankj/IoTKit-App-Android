package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.bind.BindGuideActivity;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BindCamActivity extends BaseBindActivity {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.imgV_camera_wifi_light_flash)
    ImageView imgVCameraWifiLightFlash;
    @BindView(R.id.imgV_camera_hand)
    ImageView imgVCameraHand;
    @BindView(R.id.imgV_camera_red_dot)
    ImageView imgVCameraRedDot;
    @BindView(R.id.tv_main_content)
    TextView tvMainContent;
    @BindView(R.id.tv_sub_title)
    TextView tvSubTitle;
    @BindView(R.id.fLayout_hand)
    FrameLayout fLayoutHand;
    @BindView(R.id.tv_bind_camera_tip)
    TextView tvBindCameraTip;
    @BindView(R.id.fLayout_bind_device_list_fragment_container)
    FrameLayout fLayoutBindDeviceListFragmentContainer;
    private AnimationDrawable animationDrawable;
    private AnimatorSet handFlash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_cam);
        ButterKnife.bind(this);
//        ViewUtils.addViewMargins(customToolbar, 0, (int) getResources().getDimension(R.dimen.y9), 0, 0);
        customToolbar.setBackAction(v -> finishExt());
        customToolbar.post(this::initAnimation);
        tvMainContent.setText(getIntent().getStringExtra(JConstant.KEY_ANIM_TITLE));
        tvSubTitle.setText(getIntent().getStringExtra(JConstant.KEY_ANIM_SUB_TITLE));
        tvBindCameraTip.setText(getIntent().getStringExtra(JConstant.KEY_NEXT_STEP));
    }

    protected int[] getOverridePendingTransition() {
        return new int[]{R.anim.slide_in_right, R.anim.slide_out_left};
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handFlash != null) {
            handFlash.removeAllListeners();
            handFlash.cancel();
        }
    }

    private void initAnimation() {
        handFlash = new AnimatorSet();
        handFlash.playTogether(AnimatorUtils.toCenterX(imgVCameraHand, 0),
                ObjectAnimator.ofFloat(imgVCameraHand, "alpha", 0, 1.f));
        handFlash.setDuration(800);
        handFlash.addListener(new AnimatorUtils.SimpleAnimationListener() {

            @Override
            public void onAnimationStart(Animator animator) {
//                imgVCameraRedDot.post(() -> imgVCameraRedDot.setVisibility(View.GONE));
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (animationDrawable == null)
                    animationDrawable = AnimatorUtils.onWiFiLightFlash(imgVCameraWifiLightFlash);
                if (animationDrawable != null && !animationDrawable.isRunning())
                    animationDrawable.start();
                imgVCameraRedDot.post(() -> imgVCameraRedDot.setVisibility(View.VISIBLE));
                handFlash.setStartDelay(3000);
                handFlash.start();
            }
        });
        handFlash.start();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @OnClick(R.id.tv_bind_camera_tip)
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        Intent intent = getIntent();//需要一路传下去.
        intent.setClass(this, BindGuideActivity.class);
        intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.DOG_CAMERA_NAME));
        intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bind_guide);
        intent.putExtra(JConstant.KEY_COMPONENT_NAME, this.getClass().getName());
        startActivity(intent);
    }

}
