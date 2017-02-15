package com.cylan.jiafeigou.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.cylan.jiafeigou.R;

import java.lang.ref.WeakReference;

/**
 * Created by cylan-hunt on 17-2-12.
 */

public class SmoothPercent extends View {

    private float mCurrentX;

    private RectF rectF = new RectF();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator valueAnimator;

    public SmoothPercent(Context context) {
        this(context, null);
    }

    public SmoothPercent(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothPercent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray =
                getContext().obtainStyledAttributes(attrs, R.styleable.SmoothPercent, defStyleAttr, 0);
        int pointColor = typedArray.getColor(R.styleable.SmoothPercent_sp_color, Color.RED);
        mPaint.setColor(pointColor);
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        rectF.right = mCurrentX;
        rectF.bottom = getBottom();
        canvas.drawRect(rectF, mPaint);
    }

    public void smoothSetPercent(boolean smooth, float percent) {
        if (percent < 0 || percent > 1.0f)
            return;
        if (getWidth() == 0) {
            post(() -> {
                if (smooth) {
                    initAnimator(getWidth() * percent);
                } else {
                    mCurrentX = getWidth() * percent;
                    invalidate();
                }
            });
        }
    }


    private void initAnimator(float target) {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofFloat(0, target);
        }
        valueAnimator.setStartDelay(500);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener((ValueAnimator animation) -> {
            mCurrentX = (float) animation.getAnimatedValue();
            invalidate();
            if (percentUpdateRef != null && percentUpdateRef.get() != null && getWidth() > 0)
                percentUpdateRef.get().percentUpdate(mCurrentX / getWidth());
        });
        valueAnimator.setDuration(800);
        valueAnimator.start();
    }

    private WeakReference<PercentUpdate> percentUpdateRef;

    public void setPercentUpdate(PercentUpdate percentUpdate) {
        this.percentUpdateRef = new WeakReference<>(percentUpdate);
    }

    public interface PercentUpdate {
        void percentUpdate(float percent);
    }
}
