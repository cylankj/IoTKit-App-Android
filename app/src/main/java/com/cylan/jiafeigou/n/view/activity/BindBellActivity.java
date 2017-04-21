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

import static com.cylan.jiafeigou.misc.JConstant.JUST_SEND_INFO;


public class BindBellActivity extends BaseBindActivity {

    @BindView(R.id.fLayout_flip_before)
    FrameLayout fLayoutFlipBefore;
    @BindView(R.id.imgV_wifi_light_flash)
    ImageView imgVWifiLightFlash;
    @BindView(R.id.fLayout_flip_after)
    FrameLayout fLayoutFlipAfter;
    @BindView(R.id.fLayout_flip_layout)
    FrameLayout fLayoutFlipLayout;
    @BindView(R.id.imgV_hand_left)
    ImageView imgVHandLeft;
    @BindView(R.id.imgV_hand_right)
    ImageView imgVHandRight;
    @BindView(R.id.tv_bind_doorbell_tip)
    TextView tvBindDoorbellTip;
    @BindView(R.id.imgV_wifi_light_red_dot_left)
    ImageView imgVWifiLightRedDotLeft;
    @BindView(R.id.imgV_wifi_light_red_dot_right)
    ImageView imgVWifiLightRedDotRight;
    @BindView(R.id.custom_toolbar)
    CustomToolbar mCustomToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_bell);
        ButterKnife.bind(this);
        ViewUtils.setViewMarginStatusBar(mCustomToolbar);
        initBeforeFlipAnimation();
        mCustomToolbar.setBackAction(v -> finishExt());
    }

    private AnimatorSet setHandLeft;
    private AnimatorSet setHandRight;
    private AnimatorSet setRedDotLeft;
    private AnimatorSet setRedDotRight;
    private FlipAnimation flipAnimation;

    private void initBeforeFlipAnimation() {
        setHandLeft = AnimatorUtils.onHand2Left(imgVHandLeft, new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                imgVHandLeft.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setRedDotLeft.start();
                imgVHandLeft.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imgVHandLeft.setVisibility(View.INVISIBLE);
                    }
                }, 500);
            }
        });
        setHandRight = AnimatorUtils.onHand2Right(imgVHandRight, new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                imgVHandRight.setVisibility(View.VISIBLE);
                imgVWifiLightRedDotLeft.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setRedDotRight.start();
                imgVHandRight.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imgVHandRight.setVisibility(View.INVISIBLE);
                    }
                }, 1000);
            }
        });
        setRedDotLeft = AnimatorUtils.scale(imgVWifiLightRedDotLeft, new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                imgVWifiLightRedDotLeft.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setHandRight.start();
            }
        });
        setRedDotRight = AnimatorUtils.scale(imgVWifiLightRedDotRight, new AnimatorUtils.SimpleAnimationListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                imgVWifiLightRedDotRight.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                initAnimation();
            }
        });
        setHandLeft.start();
    }


    private void initAnimation() {
        flipAnimation = new FlipAnimation(fLayoutFlipBefore, fLayoutFlipAfter);
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
    }

    private void cancelAnimation() {
        if (setHandLeft != null && setHandLeft.isRunning())
            setHandLeft.cancel();
        if (setHandRight != null && setHandRight.isRunning())
            setHandRight.cancel();
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
        startActivity(intent);
        cancelAnimation();
    }

}
