package com.cylan.jiafeigou.n.view.misc;

/**
 * Created by cylan-hunt on 16-7-13.
 */

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.lang.ref.WeakReference;


//
//横屏播放的时候，上下两条bar slideIn slideOut 动画
//

/**
 * 这一个块的 show 和hide动画
 */
public class LandLiveBarAnimDelegate {
    private WeakReference<View> weakReferenceTop;
    private WeakReference<View> weakReferenceBottom;
    private static final int STATE_SHOWING = 0;
    private static final int STATE_SHOWN = 1;
    private static final int STATE_HIDING = 2;
    private static final int STATE_HIDDEN = 3;

    private static final int MSG_AUTO_HIDE = 1;
    private int currentState = STATE_SHOWN;

    private Animator animatorShowTop;
    private Animator animatorHideTop;
    private Animator animatorHideBottom;
    private Animator animatorShowBottom;

    public LandLiveBarAnimDelegate(View viewTop, View viewBottom) {
        weakReferenceTop = new WeakReference<>(viewTop);
        weakReferenceBottom = new WeakReference<>(viewBottom);
        weakReferenceBottom.get().post(new Runnable() {
            @Override
            public void run() {
                initAnimation();
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_AUTO_HIDE) {
                hide();
            }
            return true;
        }
    });

    private void initAnimation() {
        if (weakReferenceTop == null || weakReferenceTop.get() == null) {
            return;
        }
        if (weakReferenceBottom == null || weakReferenceBottom.get() == null) {
            return;
        }
        animatorHideBottom = hideBottom();
        animatorHideTop = hideTop();
        animatorHideTop.addListener(new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                currentState = STATE_HIDING;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                currentState = STATE_HIDDEN;
            }
        });
        animatorShowBottom = showBottom();
        animatorShowTop = showTop();
        animatorShowTop.addListener(new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                currentState = STATE_SHOWING;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                currentState = STATE_SHOWN;
                handler.removeMessages(MSG_AUTO_HIDE);
                handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE, 3000L);
            }
        });
    }

    private AnimatorSet showTop() {
        Animator alpha = ObjectAnimator.ofFloat(weakReferenceTop.get(), "alpha", 0.0f, 1.0f);
        Animator animator = ObjectAnimator.ofFloat(weakReferenceTop.get(),
                "translationY",
                -weakReferenceTop.get().getHeight(),
                0);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, animator);
        set.setDuration(250);
        set.setInterpolator(new AccelerateInterpolator());
        return set;
    }

    private AnimatorSet hideTop() {
        Animator alpha = ObjectAnimator.ofFloat(weakReferenceTop.get(), "alpha", 1.0f, 0.0f);
        Animator animator = ObjectAnimator.ofFloat(weakReferenceTop.get(),
                "translationY",
                0,
                -weakReferenceTop.get().getHeight());
        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, animator);
        set.setDuration(250);
        set.setInterpolator(new AccelerateInterpolator());
        return set;
    }

    private AnimatorSet showBottom() {
//        final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        Animator alpha = ObjectAnimator.ofFloat(weakReferenceBottom.get(), "alpha", 0.0f, 1.0f);
        Animator animator = ObjectAnimator.ofFloat(weakReferenceBottom.get(),
                "translationY",
                weakReferenceBottom.get().getHeight(),
                0
        );
        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, animator);
        set.setDuration(250);
        set.setInterpolator(new AccelerateInterpolator());
        return set;
    }

    private AnimatorSet hideBottom() {
//        final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        Animator alpha = ObjectAnimator.ofFloat(weakReferenceBottom.get(), "alpha", 1.0f, 0.0f);
        Animator animator = ObjectAnimator.ofFloat(weakReferenceBottom.get(),
                "translationY",
                0,
                weakReferenceBottom.get().getHeight());
        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, animator);
        set.setDuration(250);
        set.setInterpolator(new AccelerateInterpolator());
        return set;
    }

    private void cancelAnimation() {
        if (animatorShowTop != null && animatorShowTop.isRunning()) {
            animatorShowTop.cancel();
        }
        if (animatorShowBottom != null && animatorShowBottom.isRunning()) {
            animatorShowBottom.cancel();
        }
        if (animatorHideTop != null && animatorHideTop.isRunning()) {
            animatorHideTop.cancel();
        }
        if (animatorHideBottom != null && animatorHideBottom.isRunning()) {
            animatorHideBottom.cancel();
        }
    }

    private void show() {
        if (animatorShowTop != null) {
            animatorShowTop.start();
        }
        if (animatorShowBottom != null) {
            animatorShowBottom.start();
        }
    }

    private void hide() {
        if (animatorHideTop != null) {
            animatorHideTop.start();
        }
        if (animatorHideBottom != null) {
            animatorHideBottom.start();
        }
    }

    public void startAnimation(boolean auto) {
        handler.removeCallbacksAndMessages(null);
        switch (currentState) {
            case STATE_HIDDEN:
                show();
                break;
            case STATE_HIDING:
                break;
            case STATE_SHOWING:
                break;
            case STATE_SHOWN:
                if (auto) {
                    handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE, 3000);
                } else {
                    hide();
                }
                break;
        }
    }

    public void destroy() {
        cancelAnimation();
        handler.removeCallbacksAndMessages(null);
    }
}