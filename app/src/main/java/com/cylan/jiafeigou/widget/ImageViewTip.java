package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;


/**
 * Created by hunt on 15-5-19.
 * This file aims to ....
 */
public class ImageViewTip extends ImageView {

    private float mDotRadius = 8;
    private boolean showDot = false;
    private Paint mPaint = new Paint();
    /**
     * 总共有8个位置,“米”各个角，左上角为0,顺时针增加。
     */
    private int position = 7;

    private boolean ignorePadding = false;


    public ImageViewTip(Context context) {
        super(context);
    }

    public ImageViewTip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewTip(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DotThemes, defStyle, 0);
        float radius = a.getDimensionPixelSize(R.styleable.DotThemes_Dradius, 5);
        setDotRadius(radius);
        boolean show = a.getBoolean(R.styleable.DotThemes_showDot, false);
        setShowDot(show);
        boolean ignore = a.getBoolean(R.styleable.DotThemes_ignore, false);
        setIgnorePadding(ignore);
        int position = a.getInteger(R.styleable.DotThemes_position,2);
        setPosition(position);
        a.recycle();
        init();
    }

    public void setShowDot(boolean showDot) {
        this.showDot = showDot;
        invalidate();
    }

    public boolean isShowDot() {
        return showDot;
    }

    public void setDotRadius(float mDotRadius) {
        this.mDotRadius = mDotRadius;
        invalidate();
    }

    public float getDotRadius() {
        return mDotRadius;
    }

    private void init() {
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
    }

    public void setIgnorePadding(boolean ignore) {
        this.ignorePadding = ignore;
        requestLayout();
    }

    public void setPosition(int position) {
        this.position = position;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        x[0] = ignorePadding ? getDotRadius() + getPaddingLeft() : getDotRadius();
        x[1] = getWidth() / 2;
        x[2] = ignorePadding ? getWidth() - getDotRadius() - getPaddingRight() : getWidth() - getDotRadius();
        y[0] = ignorePadding ? getDotRadius() + getPaddingTop() : getDotRadius();
        y[1] = getHeight() / 2;
        y[2] = ignorePadding ? getHeight() - getDotRadius() - getPaddingBottom() : getHeight() - getDotRadius();
    }

    private float[] x = {0f, 0f, 0f};
    private float[] y = {0f, 0f, 0f};
    private int[][] pos = {
            {0, 0}, {1, 0}, {2, 0},
            {2, 1}, {2, 2},
            {1, 2}, {0, 2}, {0, 1}};

    private PointF getPoint(int position) {
        PointF rectF = new PointF();
        rectF.x = x[pos[position][0]];
        rectF.y = y[pos[position][1]];
        return rectF;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (isShowDot()) {
            canvas.drawCircle(getPoint(position).x, getPoint(position).y, getDotRadius(), mPaint);
        }
    }
}
