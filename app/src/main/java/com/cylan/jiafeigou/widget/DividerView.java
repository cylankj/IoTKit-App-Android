package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 17-3-15.
 */

public class DividerView extends View {

    public static int ORIENTATION_HORIZONTAL = 0;
    static public int ORIENTATION_VERTICAL = 1;
    private Paint mPaint;
    private int orientation;
    private int width;
    private int height;
    private Path mPath;

    public DividerView(Context context) {
        this(context, null);
    }

    public DividerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DividerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int dashGap, dashLength, dashThickness;
        int color;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DividerView, 0, 0);
        dashGap = a.getDimensionPixelSize(R.styleable.DividerView_dv_dashGap, 5);
        dashLength = a.getDimensionPixelSize(R.styleable.DividerView_dv_dashLength, 5);
        dashThickness = a.getDimensionPixelSize(R.styleable.DividerView_dv_dashThickness, 3);
        color = a.getColor(R.styleable.DividerView_dv_color, 0xff000000);
        orientation = a.getInt(R.styleable.DividerView_dv_orientation, ORIENTATION_HORIZONTAL);
        a.recycle();
        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(dashThickness);
        mPaint.setPathEffect(new DashPathEffect(new float[]{dashLength, dashGap}, 0));
        mPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (orientation == ORIENTATION_HORIZONTAL) {
            mPath.moveTo(0, height / 2);
            mPath.lineTo(width, height / 2);
            canvas.drawPath(mPath, mPaint);
        } else {
            mPath.moveTo(width / 2, 0);
            mPath.lineTo(width / 2, height);
            canvas.drawPath(mPath, mPaint);
        }
    }
}