package com.cylan.jiafeigou.utils;

/**
 * Created by cylan-hunt on 16-12-26.
 */

import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.nineoldandroids.animation.ObjectAnimator;

import java.lang.ref.WeakReference;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public class Test implements Handler.Callback {
    private static Test instance;
    private Handler handler = new Handler(this);

    private Test() {

    }

    public static Test getInstance() {
        if (instance == null) {
            synchronized (Test.class) {
                if (instance == null) instance = new Test();
            }
        }
        return instance;
    }


    /**
     * 切进屏幕
     *
     * @param view
     * @param fromTop
     */
    public Test slideIn(View view, boolean fromTop) {
        int end = 0, start = fromTop ? -view.getHeight() : view.getHeight();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY", start, end);
        objectAnimator.setDuration(400);
        objectAnimator.start();
        return this;
    }

    /**
     * 切出屏幕
     *
     * @param view
     * @param fromTop
     */
    public Test slideOut(View view, boolean fromTop) {
        Object o = view.getTag();
        if (o != null && o instanceof ObjectAnimator) {
            ObjectAnimator animator = (ObjectAnimator) o;
            if (animator.isRunning() || animator.isStarted())
                animator.cancel();
        }
        int start = 0, end = fromTop ? -view.getHeight() : view.getHeight();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY", start, end);
        objectAnimator.setDuration(400);
        objectAnimator.start();
        view.setTag(objectAnimator);
        return this;
    }

    public void autoHide(View view, boolean fromTop,long delay) {
        float translateY = view.getTranslationY();
        view.setTag(fromTop);
        if (translateY == 0) {//初始默认状态
            slideOut(view, fromTop);
            return;
        }
        if (fromTop) {
            if (translateY == -view.getHeight()) {
                //slide In
                slideIn(view, true);
                handler.removeMessages(0);
                handler.sendMessageDelayed(getMessage(view, 0), delay);
            }
        } else {//底
            if (translateY == view.getHeight()) {//隐藏状态
                //slide In
                slideIn(view, false);
                handler.removeMessages(1);
                handler.sendMessageDelayed(getMessage(view, 1), delay);
            }
        }
    }

    private Message getMessage(View view, int msg) {
        Message m = Message.obtain();
        m.obj = new WeakReference<>(view);
        m.what = msg;
        return m;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0: {
                Object o = msg.obj;
                if (o != null && o instanceof WeakReference) {
                    Object view = ((WeakReference) o).get();
                    if (view != null && view instanceof View) {
                        if (((View) view).getTranslationY() == -((View) view).getHeight())
                            return true;
                        slideOut((View) view, true);
                    }
                }
            }
            break;
            case 1: {
                Object o = msg.obj;
                if (o != null && o instanceof WeakReference) {
                    Object view = ((WeakReference) o).get();
                    if (view != null && view instanceof View) {
                        if (((View) view).getTranslationY() == ((View) view).getHeight())
                            return true;
                        slideOut((View) view, false);
                    }
                }
            }
            break;
        }
        return true;
    }
}
