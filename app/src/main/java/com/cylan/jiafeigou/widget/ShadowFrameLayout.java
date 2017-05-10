package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;

/**
 * Created by yzd on 16-12-1.
 */

public class ShadowFrameLayout extends FrameLayout {
    Rect mRect = new Rect();
    private boolean mFixSize = false;

    public ShadowFrameLayout(Context context) {
        this(context, null);
    }

    public ShadowFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ShadowFrameLayout);
        mFixSize = array.getBoolean(R.styleable.ShadowFrameLayout_fixSize, false);
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mFixSize ) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(width / 16 * 9, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void adjustSize(boolean adjust) {
        ImageView view = (ImageView) findViewById(R.id.iv_wonderful_item_content);
        if (view != null) {
            if (adjust) {
                getLocalVisibleRect(mRect);
                view.layout(mRect.left, mRect.top, mRect.right, mRect.bottom);
            } else {
                view.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
            }
        }
    }
}
