package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.HorizontalScrollView;

public class SlowHorizontalScrollView extends HorizontalScrollView {

    private OnScrollChangedListener listener;

    public SlowHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SlowHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlowHorizontalScrollView(Context context) {
        super(context);
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY / 20);// 重点在"velocityY / 4"，这里意思是滑动速度减慢到原来四分之一的速度
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.i("Scroll", l + "");
        if (listener != null)
            listener.getScrollDistance(l);
    }

    public interface OnScrollChangedListener {
        void getScrollDistance(int distance);
    }


    public void setOnScrollChangedListener(OnScrollChangedListener l) {
        this.listener = l;
    }
}
