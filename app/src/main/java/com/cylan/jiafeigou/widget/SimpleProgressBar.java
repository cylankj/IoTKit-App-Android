package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.cylan.jiafeigou.R;


/**
 * Created by cylan-hunt on 16-7-13.
 */
public class SimpleProgressBar
        extends View implements Runnable {

    private static final String TAG = "SimpleProgress";
    private final float strokeWidth;
    private int strokeColor = Color.BLACK;
    private float pointRadius;
    /**
     * 当前角度
     */
    private float degree = 0;
    private float circleRadius = 0;
    /**
     *
     */
    private int swipeDegree = 300;
    private Paint circlePaint = new Paint();
    private Paint pointPaint = new Paint();

    private RectF circleRect = new RectF();

    private boolean run = true;

    public SimpleProgressBar(Context context) {
        this(context, null);
    }

    public SimpleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.SimpleProgressStyle,
                        defStyleAttr, 0);
        strokeWidth = a.getDimension(R.styleable.SimpleProgressStyle_progress_stroke_width, 3);
        strokeColor = a.getColor(R.styleable.SimpleProgressStyle_progress_stroke_color, Color.BLACK);
        pointRadius = a.getDimension(R.styleable.SimpleProgressStyle_progress_point_radius, 10);
        swipeDegree = a.getInt(R.styleable.SimpleProgressStyle_progress_swipe_degree, 300);
        circleRadius = a.getDimensionPixelSize(R.styleable.SimpleProgressStyle_progress_radius, 5);
        a.recycle();
        init();
    }

    private void init() {
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(strokeWidth);
        circlePaint.setColor(strokeColor);
        circlePaint.setStrokeCap(Paint.Cap.ROUND);
        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(strokeColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getSize(true, widthMeasureSpec), getSize(false, heightMeasureSpec));
    }

    private int getSize(boolean isWidth, int measureSpec) {
        int result;
        final int mode = MeasureSpec.getMode(measureSpec);
        final int size = MeasureSpec.getSize(measureSpec);
        final int padding = isWidth ? getPaddingLeft() + getPaddingRight()
                : getPaddingTop() + getPaddingBottom();
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = (int) (circleRadius * 2 + 0.5f);
            result += padding + pointRadius;
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int padding = getPaddingLeft();
        circleRect.left = padding;
        circleRect.top = padding;
        circleRect.right = getMeasuredWidth() - padding;
        circleRect.bottom = getMeasuredHeight() - padding;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        final int count = canvas.save();
        canvas.drawArc(circleRect, degree, swipeDegree, false, circlePaint);
        final float pointAngle = degree - (360 - swipeDegree) / 2;
        final float x = getMeasuredWidth() / 2
                + circleRadius * (float) Math.cos(pointAngle * Math.PI / 180);
        final float y = getMeasuredHeight() / 2
                - circleRadius * (float) Math.sin((pointAngle + 180.0f) * Math.PI / 180);
        canvas.drawCircle(x, y, pointRadius, pointPaint);
        canvas.restoreToCount(count);
        if (run)
            post(this);
    }

    @Override
    public void run() {
        degree += 5;
        invalidate();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (visibility == VISIBLE) {
            run = true;
            invalidate();
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    public void dismiss() {
        run = false;
    }
}
