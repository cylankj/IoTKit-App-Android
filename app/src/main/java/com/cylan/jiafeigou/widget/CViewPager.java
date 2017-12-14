package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by yanzhendong on 2017/12/13.
 */

public class CViewPager extends ViewPager {
    public CViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean locked = false;

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !locked && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        return !locked && super.onTouchEvent(ev);
    }
}
