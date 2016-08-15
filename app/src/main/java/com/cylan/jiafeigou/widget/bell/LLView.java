package com.cylan.jiafeigou.widget.bell;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.cylan.jiafeigou.R;

import java.lang.ref.WeakReference;


/**
 * Created by cylan-hunt on 16-8-9.
 */
public class LLView extends View {


    private float itemInterval = 10;

    private int maxCount = 4;
    /**
     * 小点的颜色
     */
    private int sColor = Color.BLACK;

    /**
     * 大点的内圆颜色
     */
    private int bIColor = Color.YELLOW;
    /**
     * 大点的外圆颜色
     */
    private int bOColor = Color.BLUE;

    /**
     * 大点的内半径
     */
    private float bIRadius = 5;

    /**
     * 大点的外半径
     */
    private float bORadius = 10;
    private final float fOutRadius;

    /**
     * 小点的半径
     */
    private float sRadius = 2;

    /**
     * 反向
     */
    private boolean reverse;

    private int currentLightIndex;

    private ValueAnimator valueAnimator;

    private Paint paint = new Paint();

    public LLView(Context context) {
        this(context, null);
    }

    public LLView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LLView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Resources.Theme theme = context.getTheme();
        /*
         * Look the appearance up without checking first if it exists because
         * almost every TextView has one and it greatly simplifies the logic
         * to be able to parse the appearance first and then let specific tags
         * for this View override it.
         */
        TypedArray a = theme.obtainStyledAttributes(attrs,
                R.styleable.LLViewStyle, defStyleAttr, defStyleAttr);
        sColor = a.getColor(R.styleable.LLViewStyle_ll_small_color, Color.BLACK);
        bOColor = a.getColor(R.styleable.LLViewStyle_ll_big_outer_color, Color.BLACK);
        bIColor = a.getColor(R.styleable.LLViewStyle_ll_big_inner_color, Color.BLUE);

        bORadius = a.getDimension(R.styleable.LLViewStyle_ll_big_outer_radius, bORadius);
        fOutRadius = bORadius;
        bIRadius = a.getDimension(R.styleable.LLViewStyle_ll_big_inner_radius, bIRadius);
        sRadius = a.getDimension(R.styleable.LLViewStyle_ll_small_radius, sRadius);
        itemInterval = a.getDimension(R.styleable.LLViewStyle_ll_small_interval, itemInterval);


        reverse = a.getBoolean(R.styleable.LLViewStyle_ll_reverse, false);
        a.recycle();
        init();
    }

    private void init() {
        paint.setAntiAlias(true);
    }

    private void setCurrentLightIndex(int index) {
        this.currentLightIndex = index;
    }

    /**
     * 重新设置
     *
     * @param ratio
     */
    public void setbORadiusRatio(float ratio) {
        this.bORadius = fOutRadius + ratio * computeScale();
        invalidate();
    }

    private float computeScale() {
        return ((getMeasuredHeight() - fOutRadius * 2) / 2.0f);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        startAnimation(visibility == VISIBLE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initAnimator();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getSize(false, widthMeasureSpec), getSize(true, heightMeasureSpec));
    }

    private int getSize(boolean isH, int spec) {
        final int mode = MeasureSpec.getMode(spec);
        final int size = MeasureSpec.getSize(spec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return isH ? size : computeWidth(size);
            case MeasureSpec.AT_MOST://wrap_content
                return isH ? (int) (bORadius * 2 + getPaddingBottom() + getPaddingTop())
                        : (int) (getPaddingLeft() + getPaddingRight()
                        + bORadius * 2
                        + sRadius * maxCount * 2
                        + (maxCount - 1) * itemInterval);
            case MeasureSpec.UNSPECIFIED:
                return 0;
            default:
                return 0;
        }
    }

    private int computeWidth(final int size) {
        return (int) (getPaddingLeft() + getPaddingRight()
                + size
                + sRadius * maxCount * 2
                + (maxCount - 1) * itemInterval);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawFourSmallCircle(canvas);
        drawBigCircle(canvas);
    }


    private void drawFourSmallCircle(Canvas canvas) {
        for (int i = 0; i < maxCount; i++) {
            if (i == currentLightIndex) {
                paint.setColor(bIColor);
            } else paint.setColor(sColor);
            canvas.drawCircle(getSmallCircleX(i), getMeasuredHeight() / 2, sRadius, paint);
        }
    }

    private void drawBigCircle(Canvas canvas) {
        //外圆
        paint.setColor(bOColor);
        canvas.drawCircle(getBigCircleX(), getMeasuredHeight() / 2, bORadius, paint);
        //内圆
        paint.setColor(bIColor);
        canvas.drawCircle(getBigCircleX(), getMeasuredHeight() / 2, bIRadius, paint);
    }

    private float getSmallCircleX(final int index) {
        final int factor = reverse ? -1 : 1;
        return getBigCircleX()
                + factor * getMeasuredHeight() / 2
                + factor * sRadius
                + factor * (itemInterval + sRadius) * index;
    }

    private float getBigCircleX() {
        final int x = getMeasuredHeight() / 2;
        return reverse ? getMeasuredWidth() - x : x;
    }


    private void startAnimation(boolean start) {
        if (start) {
            if (valueAnimator == null)
                initAnimator();
            if (valueAnimator != null && valueAnimator.isRunning())
                return;
            valueAnimator.start();
        } else {
            if (valueAnimator == null || !valueAnimator.isRunning())
                return;
            valueAnimator.cancel();
        }
    }


    private void initAnimator() {
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofInt(0, maxCount);
            valueAnimator.setDuration(400);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new Update(this));
            valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        }

    }

    private static class Update implements ValueAnimator.AnimatorUpdateListener {
        WeakReference<LLView> viewWeakReference;

        public Update(LLView view) {
            viewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (viewWeakReference != null && viewWeakReference.get() != null) {
                viewWeakReference.get().setCurrentLightIndex((int) animation.getAnimatedValue());
                viewWeakReference.get().invalidate();
            }
        }
    }
}
