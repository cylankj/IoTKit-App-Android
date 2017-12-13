package com.cylan.jiafeigou.widget.page;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.cylan.jiafeigou.R;

/**
 * @author hds
 * @date 17-11-1
 */

public class EViewPager extends ViewPager {
    private boolean isNeedWrap = false;
    public EnableScrollListener enableScrollListener;
    private boolean isLocked = false;

    public EViewPager(Context context) {
        super(context);
    }

    public EViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.EViewPager, 0, 0);
        this.isNeedWrap = a.getBoolean(R.styleable.EViewPager_vp_need_wrap, false);
        a.recycle();
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (isLocked) return false;
        if (v != this && v instanceof ViewPager) {
            int currentItem = ((ViewPager) v).getCurrentItem();
            int countItem = ((ViewPager) v).getAdapter().getCount();
            if ((currentItem == (countItem - 1) && dx < 0) || (currentItem == 0 && dx > 0)) {
                return true;
            }
            return true;
        }
        return super.canScroll(v, checkV, dx, x, y);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isNeedWrap) {
            int height = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                int h = child.getMeasuredHeight();
                if (h > height) {
                    height = h;
                }
            }
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @param view        the base view with already measured height
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec, View view) {
        int result = 0;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            // set the height from the base view if available
            if (view != null) {
                result = view.getMeasuredHeight();
            }
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }


    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLocked) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (isConsumedOutside(event)) {
                    return false;
                }
                if (isLocked) {
                    return false;
                }
                return handleIntercept(event);
            }
            default:
                if (isLocked) {
                    return false;
                }
                return handleIntercept(event);
        }
    }

    private boolean isConsumedOutside(MotionEvent event) {
        return enableScrollListener != null && enableScrollListener.enable(event);
    }

    private boolean handleIntercept(MotionEvent event) {
        try {
            return super.onInterceptTouchEvent(event);
        } catch (IllegalArgumentException e) {
            Log.d("failed", "failed:" + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * @param enable: false to disable scroll
     */
    public void setPagingEnabled(boolean enable) {
        setLocked(!enable);
    }


    public void setPagingScrollListener(EnableScrollListener listener) {
        this.enableScrollListener = listener;
    }

    public interface EnableScrollListener {
        /**
         * 滚动
         *
         * @param event
         * @return
         */
        boolean enable(MotionEvent event);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, false);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item, false);
    }
}
