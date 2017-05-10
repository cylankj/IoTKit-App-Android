package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 17-3-15.
 */

public class AspectRatioImageView extends AppCompatImageView {
    private int ratio;

    public AspectRatioImageView(Context context) {
        this(context, null);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray at = context.obtainStyledAttributes(attrs, R.styleable.AspectImageViewStyle);
        this.ratio = at.getInt(R.styleable.AspectImageViewStyle_as_ratio, 1);
        at.recycle();
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) ((float) width / this.ratio);
        setMeasuredDimension(width, height);
    }
}
