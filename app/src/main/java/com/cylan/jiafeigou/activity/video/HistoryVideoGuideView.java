package com.cylan.jiafeigou.activity.video;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.SlowHorizontalScrollView;

public class HistoryVideoGuideView {

    ImageView img;
    private SlowHorizontalScrollView hsview;
    private View guide;
    private ViewGroup mRoot;
    private ObjectAnimator mAnimator;

    /**
     * @param view
     * @param context
     * @param hsview
     */
    public HistoryVideoGuideView(ViewGroup view, Context context, final SlowHorizontalScrollView hsview) {
        this.hsview = hsview;
        this.mRoot = view;

        guide = LayoutInflater.from(context).inflate(R.layout.view_historyvideo_guide, null);
        android.view.ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        view.addView(guide, params);
        img = (ImageView) guide.findViewById(R.id.guide_finger);

        mAnimator = ObjectAnimator.ofFloat(img, "translationX", 0f, -30f);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setDuration(2000);
        mAnimator.setRepeatCount(-1);
        mAnimator.setRepeatMode(ObjectAnimator.RESTART);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });

        mAnimator.start();

        guide.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                clearAnim();
            }
        });
    }

    public void clearAnim() {
        mAnimator.cancel();
        if (img != null)
            img.clearAnimation();
        mRoot.removeView(guide);
    }


}