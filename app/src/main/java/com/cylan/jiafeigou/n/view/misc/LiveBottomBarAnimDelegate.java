package com.cylan.jiafeigou.n.view.misc;

/**
 * Created by cylan-hunt on 16-7-13.
 */

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.lang.ref.WeakReference;


//
// 安全防护   直播|5/16 12:30   全屏
//

/**
 * 这一个块的 show 和hide动画
 */
public class LiveBottomBarAnimDelegate {
    private WeakReference<View> weakReference;
    private static final int STATE_SHOWING = 0;
    private static final int STATE_SHOWN = 1;
    private static final int STATE_HIDING = 2;
    private static final int STATE_HIDDEN = 3;

    private static final int MSG_AUTO_HIDE = 1;
    private int currentState = STATE_SHOWN;

    private Animator animatorShow;
    private Animator animatorHide;

    public LiveBottomBarAnimDelegate(View view) {
        weakReference = new WeakReference<>(view);
        view.post(new Runnable() {
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
        if (weakReference == null || weakReference.get() == null)
            return;
        if (animatorShow == null) {
            animatorShow = ObjectAnimator.ofFloat(weakReference.get(), "translationY", weakReference.get().getHeight(), 0.0f);
            animatorShow.setDuration(250);
            animatorShow.setInterpolator(new AccelerateInterpolator());
            animatorShow.addListener(new AnimatorUtils.SimpleAnimationListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    currentState = STATE_SHOWING;
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    currentState = STATE_SHOWN;
                    handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE, 3000);
                }
            });
        }
        if (animatorHide == null) {
            animatorHide = ObjectAnimator.ofFloat(weakReference.get(), "translationY", 0.0f, weakReference.get().getHeight());
            animatorHide.setDuration(250);
            animatorHide.setInterpolator(new LinearInterpolator());
            animatorHide.addListener(new AnimatorUtils.SimpleAnimationListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    currentState = STATE_HIDING;
                    if (weakReference != null && weakReference.get() != null) {
                        weakReference.get().setPivotY(weakReference.get().getBottom());
                    }
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    currentState = STATE_HIDDEN;
                }
            });
        }
    }

    private void cancelAnimation() {
        if (animatorShow != null && animatorShow.isRunning())
            animatorShow.cancel();
        if (animatorHide != null && animatorHide.isRunning())
            animatorHide.cancel();
    }

    private void show() {
        if (animatorShow != null && !animatorShow.isRunning()) animatorShow.start();
    }

    private void hide() {
        if (animatorHide != null && !animatorHide.isRunning()) animatorHide.start();
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
                if (auto)
                    handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE, 3000);
                else
                    hide();
                break;
        }
    }

    public void destroy() {
        cancelAnimation();
        handler.removeCallbacksAndMessages(null);
    }
}