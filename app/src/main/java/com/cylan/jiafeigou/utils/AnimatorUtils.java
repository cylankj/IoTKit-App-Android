package com.cylan.jiafeigou.utils;

import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.superlog.SLog;

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
     * @param offset   是否有位移（做抖动的时使用）
     * @param duration 持续时间
     */
    public static void viewTranslationY(View view, boolean isShow, long delay, float start, float end, float offset, int duration) {
        ObjectAnimator an;
        if (isShow) {
            an = ObjectAnimator.ofFloat(view, "translationY", start, offset, end);
//            an.setInterpolator(new BounceInterpolator());
            an.setInterpolator(new AnticipateOvershootInterpolator());
        } else {
            an = ObjectAnimator.ofFloat(view, "translationY", start, end);
        }
        an.addListener(new showViewListener(view, isShow));
        if (delay > 0) {
            an.setStartDelay(delay);
        }
        an.setDuration(duration).start();
    }


    /**
     * view 的水平移动动画
     *
     * @param view     view
     * @param isShow   是否显示
     * @param delay    延迟
     * @param start    开始位置
     * @param end      结束位置
     * @param offset   是否有位移（做抖动的时使用）
     * @param duration 持续时间
     */
    public static void viewTranslationX(View view, boolean isShow, long delay, float start, float end, float offset, int duration) {
        ObjectAnimator an;
        if (isShow) {
            an = ObjectAnimator.ofFloat(view, "translationX", start, offset, end);
            an.setInterpolator(new BounceInterpolator());
        } else {
            an = ObjectAnimator.ofFloat(view, "translationX", start, end);
        }
        an.addListener(new showViewListener(view, isShow));
        if (delay > 0) {
            an.setStartDelay(delay);
        }
        an.setDuration(duration).start();
    }


    /**
     * 以中心为原点缩放view
     *
     * @param view
     * @param isShow
     */
    public static void ViewScaleCenter(View view, boolean isShow, int duration, int delay) {
        AnimatorSet set = new AnimatorSet();
        if (isShow) {
            SLog.e("ViewScaleCenter");
            set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f),
                    ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f),
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f));
        } else {
            SLog.e("ViewScaleCenter------------ fasle ");
            set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f),
                    ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f),
                    ObjectAnimator.ofFloat(view, "alpha", 1f, 0f));
        }
        SLog.e("delay: " + delay);
        set.setStartDelay(delay);
        set.addListener(new showViewListener(view, isShow));
        set.setDuration(duration).start();
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


}
