package com.cylan.jiafeigou.activity.main;

import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class MyOnTouchListener implements OnTouchListener {

    private boolean isScale = false;

    public MyOnTouchListener() {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!isScale) {
                    ButtonScaleAnimation(v, 1.0f, 1.2f);
                    isScale = true;
                }
                return false;
            }
            case MotionEvent.ACTION_UP: {
                if (isScale) {
                    ButtonScaleAnimation(v, 1.2f, 1.0f);
                    isScale = false;
                }
                return false;
            }

        }
        return false;

    }

    private void ButtonScaleAnimation(View v, float from, float to) {
        ObjectAnimator.ofFloat(v, "scaleX", from, to).setDuration(50).start();
        ObjectAnimator.ofFloat(v, "scaleY", from, to).setDuration(50).start();
    }
}
