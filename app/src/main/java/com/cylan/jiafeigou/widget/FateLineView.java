package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import com.cylan.jiafeigou.R;


/**
 * Created by hunt on 15-12-8.
 */
public class FateLineView extends View {

    private int dashLineColor = Color.BLACK;
    private float dashLineWidth = 1;
    private int outerCircleColor = Color.WHITE;
    private int innerCircleColor = Color.BLACK;
    private float innerCircleRadius = 5;
    private float outerCircleStrokeWidth = 4;
    private float outerCircleRadius = 10;
    private float outerCircleY = 20.0f;
    private Paint dashLinePaint;
    private Paint circlePaint;

    private boolean centerVertical;
    Path dashPath = new Path();

    public FateLineView(Context context) {
        this(context, null);
    }

    public FateLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FateLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray
                at = context.obtainStyledAttributes(attrs, R.styleable.FateLineViewStyle);
        this.dashLineColor = at.getColor(R.styleable.FateLineViewStyle_dashLineColor, Color.BLACK);
        this.outerCircleColor = at.getColor(R.styleable.FateLineViewStyle_outerCircleColor, outerCircleColor);
        this.innerCircleColor = at.getColor(R.styleable.FateLineViewStyle_innerCircleColor, innerCircleColor);
        this.dashLineWidth = at.getDimension(R.styleable.FateLineViewStyle_dashLineWidth, dashLineWidth);
        this.innerCircleRadius = at.getDimension(R.styleable.FateLineViewStyle_innerCircleRadius, innerCircleRadius);
        this.outerCircleRadius = at.getDimension(R.styleable.FateLineViewStyle_outerCircleRadius, outerCircleRadius);
        this.outerCircleY = at.getDimension(R.styleable.FateLineViewStyle_outerCircleY, -1);
        this.outerCircleStrokeWidth = at.getDimension(R.styleable.FateLineViewStyle_outerCircleStrokeWidth, outerCircleStrokeWidth);
        this.outerCircleY = at.getDimension(R.styleable.FateLineViewStyle_outerCircleY, outerCircleY);
        at.recycle();
        centerVertical = outerCircleY == -1;
//        innerCircleRadius = dip2px(getContext(), innerCircleRadius);
//        dashLineWidth = dip2px(getContext(), dashLineWidth);
//        outerCircleRadius = dip2px(getContext(), outerCircleRadius);
//        outerCircleY = dip2px(getContext(), outerCircleY);
//        innerCircleStrokeWidth = dip2px(getContext(), innerCircleStrokeWidth);
        init();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static float dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (dpValue * scale + 0.5f);
    }

    private void init() {
        dashLinePaint = new Paint();
        dashLinePaint.setAntiAlias(true);
        dashLinePaint.setColor(dashLineColor);
        //画虚线，必须设置Stroke模式
        dashLinePaint.setStyle(Paint.Style.STROKE);
        dashLinePaint.setStrokeWidth(dashLineWidth);
        PathEffect effects = new DashPathEffect(new float[]{7, 7, 7, 7}, 1);
        dashLinePaint.setPathEffect(effects);
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(outerCircleColor);
    }

    public void setInnerCircleRadius(int innerCircleRadius) {
        this.innerCircleRadius = innerCircleRadius;
    }

    public void setOuterCircleRadius(int outerCircleRadius) {
        this.outerCircleRadius = outerCircleRadius;
    }

    public void setDashLineColor(int backBarColor) {
        this.dashLineColor = backBarColor;
        postInvalidate();
    }

    public void setDashLineWidth(int dashLineWidth) {
        this.dashLineWidth = dashLineWidth;
        postInvalidate();
    }

    public void setOuterCircleColor(int outerCircleColor) {
        this.outerCircleColor = outerCircleColor;
        circlePaint.setColor(outerCircleColor);
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec, true), MeasureSpec.getSize(heightMeasureSpec));
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int measure(int measureSpec, boolean isWidth) {
        int result;
        final int mode = MeasureSpec.getMode(measureSpec);
        final int size = MeasureSpec.getSize(measureSpec);
        final int padding = isWidth ? getPaddingLeft() + getPaddingRight() + (int) outerCircleRadius : getPaddingTop() + getPaddingBottom() + (int) outerCircleRadius;
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = isWidth ? (int) (innerCircleRadius * 2) : getSuggestedMinimumHeight();
            result += padding + 0.5;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        int rectTop = getPaddingTop();
        int rectBottom = getMeasuredHeight() - getPaddingBottom();
        float circleTop = centerVertical ? getMeasuredHeight() / 2 : outerCircleY + outerCircleRadius;
        final int count = canvas.save();
        //虚线
        dashPath.moveTo(getMeasuredWidth() / 2, rectTop);
        dashPath.lineTo(getMeasuredWidth() / 2, rectBottom);
        canvas.drawPath(dashPath, dashLinePaint);
        //外圆
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(outerCircleStrokeWidth);
        circlePaint.setColor(Color.WHITE);
        canvas.drawCircle(getMeasuredWidth() / 2,
                circleTop,
                innerCircleRadius + outerCircleStrokeWidth - 0.5f,
                circlePaint);

        //外圆
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(outerCircleStrokeWidth);
        circlePaint.setColor(outerCircleColor);
        canvas.drawCircle(getMeasuredWidth() / 2,
                circleTop,
                innerCircleRadius + outerCircleStrokeWidth / 2 - 0.5f,
                circlePaint);
        //内圆
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(innerCircleColor);
        canvas.drawCircle(getMeasuredWidth() / 2,
                circleTop,
                innerCircleRadius + 0.5f,
                circlePaint);

        canvas.restoreToCount(count);
    }

}
