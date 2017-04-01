package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.bind.BindGuideActivity;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.nineoldandroids.animation.Animator;

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
    private Animator animator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_cam);
        ButterKnife.bind(this);
        ViewUtils.setViewMarginStatusBar(customToolbar);
        customToolbar.setBackAction(v -> finishExt());
        initAnimation();
    }

    protected int[] getOverridePendingTransition() {
        return new int[]{R.anim.slide_in_right, R.anim.slide_out_left};
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initAnimation() {
        animator = AnimatorUtils.onHandMoveAndFlash(imgVCameraHand, imgVCameraRedDot, imgVCameraWifiLightFlash);
        animator.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (animator != null && animator.isRunning())
            animator.cancel();
    }

    @OnClick(R.id.tv_bind_camera_tip)
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        Intent intent = new Intent(this, BindGuideActivity.class);
        intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.DOG_CAMERA_NAME));
        startActivity(intent);
        cancelAnimation();
    }

    private void cancelAnimation() {
        if (animator != null && animator.isRunning())
            animator.cancel();
    }

}
