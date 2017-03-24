package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by hunt on 16-5-24.
 */

public class CustomViewPager extends ViewPager {
    private boolean isPagingEnabled = true;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (isPagingEnabled && enableScrollListener != null && enableScrollListener.enable(event)) {
                    return super.onTouchEvent(event);
                } else return false;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (isPagingEnabled && enableScrollListener != null && enableScrollListener.enable(event)) {
                    return super.onInterceptTouchEvent(event);
                } else return false;
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    /**
     * @param enable: false to disable scroll
     */
    public void setPagingEnabled(boolean enable) {
        this.isPagingEnabled = enable;
    }

    private EnableScrollListener enableScrollListener;

    public void setPagingScrollListener(EnableScrollListener listener) {
        this.enableScrollListener = listener;
    }

    public interface EnableScrollListener {
        boolean enable(MotionEvent event);
    }
}
