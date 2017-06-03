package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.anim.FlipAnimation;
import com.cylan.jiafeigou.n.view.bind.BindGuideActivity;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class BindBellActivity extends BaseBindActivity {
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    @BindView(R.id.imgV_power_light)
    ImageView imgVPowerLight;
    @BindView(R.id.imgV_wifi_light)
    ImageView imgVWifiLight;
    @BindView(R.id.imgV_hand)
    ImageView imgVHand;
    @BindView(R.id.fLayout_flip_before)
    FrameLayout fLayoutFlipBefore;
    @BindView(R.id.imgV_wifi_light_flash)
    ImageView imgVWifiLightFlash;
    @BindView(R.id.fLayout_flip_after)
    FrameLayout fLayoutFlipAfter;
    @BindView(R.id.fLayout_flip_layout)
    FrameLayout fLayoutFlipLayout;
    @BindView(R.id.tv_bind_doorbell_tip)
    TextView tvBindDoorbellTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_bell);
        ButterKnife.bind(this);
        ViewUtils.setViewMarginStatusBar(customToolbar);
        customToolbar.setBackAction(v -> finishExt());
        customToolbar.post(this::initBeforeFlipAnimation);
    }

    private AnimatorSet setHandLeft;
    private AnimatorSet setRedDotLeft;
    private AnimatorSet setRedDotRight;

    private void initBeforeFlipAnimation() {
        setHandLeft = AnimatorUtils.onHand2Left(imgVHand, null);
        AnimatorSet setHandRight = AnimatorUtils.onHand2Right(imgVHand, null);
        setRedDotLeft = AnimatorUtils.scale(imgVPowerLight, new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                imgVPowerLight.setVisibility(View.VISIBLE);
            }
        });
        setRedDotRight = AnimatorUtils.scale(imgVWifiLight, new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                imgVWifiLight.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                prepareFlipAnimation();
            }
        });
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(setHandLeft, setRedDotLeft, setHandRight, setRedDotRight);
        imgVHand.setVisibility(View.VISIBLE);
        set.start();
    }


    private void prepareFlipAnimation() {
        FlipAnimation flipAnimation = new FlipAnimation(fLayoutFlipBefore, fLayoutFlipAfter);
        fLayoutFlipLayout.startAnimation(flipAnimation);
        flipAnimation.setStartOffset(1000);
        flipAnimation.setAnimationListener(new FlipAnimation.SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                Drawable drawable = imgVWifiLightFlash.getDrawable();
                if (drawable != null && drawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) drawable).start();
                }
            }
        });
        flipAnimation.start();
    }

    private void stopPreFlipAnimation() {
        if (setHandLeft != null && setHandLeft.isRunning())
            setHandLeft.cancel();
        if (setRedDotLeft != null && setRedDotLeft.isRunning())
            setRedDotLeft.cancel();
        if (setRedDotRight != null && setRedDotRight.isRunning())
            setRedDotRight.cancel();
    }

    @OnClick(R.id.tv_bind_doorbell_tip)
    public void onClick() {
        Intent intent = getIntent();
        intent.setClass(this, BindGuideActivity.class);
        intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.CALL_CAMERA_NAME));
        intent.putExtra(JConstant.KEY_COMPONENT_NAME, this.getClass().getName());
        startActivity(intent);
        stopPreFlipAnimation();
    }

}
