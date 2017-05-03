package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.anim.FlipAnimation;
import com.cylan.jiafeigou.n.view.bind.BindGuideActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class BindRsCamActivity extends BaseBindActivity {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.fLayout_flip_before)
    FrameLayout fLayoutFlipBefore;
    @BindView(R.id.img_blink)
    ImageView imgBlink;
    @BindView(R.id.fLayout_flip_after)
    FrameLayout fLayoutFlipAfter;
    @BindView(R.id.img_needle)
    ImageView imgNeedle;
    @BindView(R.id.fLayout_flip_layout)
    FrameLayout fLayoutFlipLayout;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_rs_cam);
        ButterKnife.bind(this);
        ViewUtils.setViewMarginStatusBar(customToolbar);
        customToolbar.setBackAction(v -> finishExt());
        customToolbar.postDelayed(this::prepareAnimation, 500);
    }

    @OnClick(R.id.tv_bind_camera_tip)
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        Intent intent = getIntent();
        intent.setClass(this, BindGuideActivity.class);
        intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.RuiShi_Name));
        startActivity(intent);
        if (subscription != null) subscription.unsubscribe();
    }

    private void prepareAnimation() {
//        ObjectAnimator offsetX = ;
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(imgNeedle, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(imgNeedle, "translationX", 0.0f, 200.0f));
        set.setDuration(800);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                prepareFlipAnimation();
                imgNeedle.setVisibility(View.GONE);
            }
        });
        set.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null) subscription.unsubscribe();
    }

    private void prepareFlipAnimation() {
        FlipAnimation flipAnimation = new FlipAnimation(fLayoutFlipBefore, fLayoutFlipAfter);
        fLayoutFlipLayout.startAnimation(flipAnimation);
        flipAnimation.setStartOffset(1000);
        flipAnimation.setAnimationListener(new FlipAnimation.SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                justBlink();
            }
        });
        flipAnimation.start();
    }

    private int count;

    private void justBlink() {
        if (subscription != null) subscription.unsubscribe();
        subscription = Observable.interval(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    imgBlink.setVisibility((count++) % 2 == 0 ? View.VISIBLE : View.INVISIBLE);
                }, AppLogger::e);
    }
}
