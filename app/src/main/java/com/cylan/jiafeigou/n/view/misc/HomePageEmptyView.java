package com.cylan.jiafeigou.n.view.misc;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by cylan-hunt on 16-8-2.
 */
public class HomePageEmptyView extends EmptyView {

    private View view;

    public HomePageEmptyView(Context context, int layoutId) {
        super(context, layoutId);
        view = reInit();
        initAnimator();
    }

    private void initAnimator() {
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f);
        showAnimator.setDuration(400);
        showAnimator.setInterpolator(new LinearInterpolator());
        showAnimator.addListener(new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                view.setAlpha(0.0f);
                view.setVisibility(View.VISIBLE);
            }
        });
        showAnimation.playTogether(showAnimator);

        ObjectAnimator hideAnimator = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f);
        hideAnimator.setDuration(400);
        hideAnimator.setInterpolator(new LinearInterpolator());
        hideAnimator.addListener(new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.GONE);
            }
        });
        hideAnimation.playTogether(hideAnimator);
    }

    @Override
    public void addView(ViewGroup viewGroup, ViewGroup.LayoutParams lp) {
        viewGroup.addView(reInit(), 0, lp);
    }
}
