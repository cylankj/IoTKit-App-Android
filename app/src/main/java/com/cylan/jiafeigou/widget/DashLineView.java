package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

/**
 * 作者：zsl
 * 创建时间：2016/10/20
 * 描述：
 */
public class DashLineView extends View {

    private Paint dashLinePaint;
    private Path dashPath;

    public DashLineView(Context context) {
        this(context, null);
        init();
    }

    public DashLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public DashLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dashPath = new Path();
        dashLinePaint = new Paint();
        dashLinePaint.setAntiAlias(true);
        dashLinePaint.setColor(Color.parseColor("#e8e8e8"));
        dashLinePaint.setStyle(Paint.Style.STROKE);
        dashLinePaint.setStrokeWidth(4);
        PathEffect effects = new DashPathEffect(new float[]{7, 7, 7, 7}, 1);
        dashLinePaint.setPathEffect(effects);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false));
    }

    private int measure(int measureSpec, boolean isWidth) {
        int result;
        final int mode = MeasureSpec.getMode(measureSpec);
        final int size = MeasureSpec.getSize(measureSpec);
        final int padding = isWidth ? getPaddingLeft() + getPaddingRight() + (int) 10 : getPaddingTop() + getPaddingBottom() + (int) 10;
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = isWidth ? (int) (6 * 2) : getSuggestedMinimumHeight();
            result += padding + 0.5;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int rectTop = getPaddingTop();
        int rectBottom = getMeasuredHeight() - getPaddingBottom();
        //虚线
        dashPath.moveTo(getMeasuredWidth() / 2, rectTop);
        dashPath.lineTo(getMeasuredWidth() / 2, rectBottom);
        canvas.drawPath(dashPath, dashLinePaint);
    }
}
