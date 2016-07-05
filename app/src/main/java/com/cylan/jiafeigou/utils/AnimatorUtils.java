package com.cylan.jiafeigou.utils;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by lxh on 16-6-16.
 */
public class AnimatorUtils {

    /**
     * view 的垂直移动动画
     *
     * @param view     view
     * @param isShow   是否显示
     * @param delay    延迟
     * @param start    开始位置
     * @param end      结束位置
     * @param duration 持续时间
     */
    public static void viewTranslationY(View view, boolean isShow, long delay, float start, float end, int duration) {
        ObjectAnimator an;
        if (isShow) {
            an = ObjectAnimator.ofFloat(view, "translationY", start, end);
            an.setInterpolator(new OvershootInterpolator());
        } else {
            an = ObjectAnimator.ofFloat(view, "translationY", start, end);
        }
        an.addListener(new showViewListener(view, isShow));
        if (delay > 0) {
            an.setStartDelay(delay);
        }
        an.setDuration(duration).start();
    }

    public static void slide(final View target) {
        int height = target.getHeight();
        if (height == 0) height = 300;
        final float start = target.isShown() ? 0.0f : height;
        final float end = target.isShown() ? height : 0.0f;
        final boolean shouldGone = target.isShown();
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(target, "translationY", start, end));
        set.setDuration(200);
        set.setInterpolator(new AccelerateInterpolator());
        set.addListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (!target.isShown())
                    target.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (shouldGone)
                    target.setVisibility(View.GONE);
            }
        });
        set.start();
    }

    private static class SimpleAnimationListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {

        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

    /**
     * view 的水平移动动画
     *
     * @param view     view
     * @param isShow   是否显示
     * @param delay    延迟
     * @param start    开始位置
     * @param end      结束位置
     * @param duration 持续时间
     */
    public static void viewTranslationX(View view, boolean isShow, long delay, float start, float end, int duration) {
        ObjectAnimator an;
        if (isShow) {
            an = ObjectAnimator.ofFloat(view, "translationX", start, end);
            an.setInterpolator(new OvershootInterpolator());
        } else {
            an = ObjectAnimator.ofFloat(view, "translationX", start, end);
        }
        an.addListener(new showViewListener(view, isShow));
        an.setDuration(duration);
        if (delay > 0) {
            an.setStartDelay(delay);
        }
        an.start();

    }


    /**
     * 以中心为原点缩放view
     *
     * @param view
     * @param isShow
     */
    public static void viewScaleCenter(View view, boolean isShow, int duration, int delay) {
        AnimatorSet set = new AnimatorSet();
        if (isShow) {
            set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f),
                    ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f),
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f));
        } else {
            set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f),
                    ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f),
                    ObjectAnimator.ofFloat(view, "alpha", 1f, 0f));
        }
        set.setDuration(duration).addListener(new showViewListener(view, isShow));
        if (delay > 0) {
            set.setStartDelay(delay);
        }
        set.start();
    }


    public static void viewAlpha(View view, boolean isShow, int duration, int delay) {
        ObjectAnimator an;
        if (isShow) {
            an = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        } else {
            an = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        }

        an.setDuration(duration).addListener(new showViewListener(view, isShow));
        if (delay > 0) {
            an.setStartDelay(delay);
        }
        an.start();
    }


    /**
     * 动画监听器
     */
    static class showViewListener implements Animator.AnimatorListener {

        private boolean isShow;
        private View view;

        public showViewListener(View view, boolean isShow) {
            this.isShow = isShow;
            this.view = view;
        }

        // 在开始时，先显示view
        @Override
        public void onAnimationStart(Animator animator) {
            if (isShow) {
                view.setVisibility(View.VISIBLE);
            }
        }

        //如果要隐藏的view 在结束时隐藏
        @Override
        public void onAnimationEnd(Animator animator) {
            if (!isShow) {
                view.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

    public static void onSimpleBounceUpIn(View target, final long duration, final long delay) {
        target.setTranslationY(800);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(target, "translationY", 800, -30.0F, 0.0F));
//                ObjectAnimator.ofFloat(target, "alpha", 0.0F, 1.0F, 1.0F, 1.0F));
        set.setDuration(duration);
        set.setStartDelay(delay);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
    }

    public static void onSimpleTangle(final long duration, final long delay, View target) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(target, "scaleY", 1.0F, 1.1F, 1.0F),
                ObjectAnimator.ofFloat(target, "scaleX", 1.0F, 1.1F, 1.0F));
        set.setDuration(duration);
        set.setInterpolator(new OvershootInterpolator());
        set.setStartDelay(delay);
        set.start();
    }

}
