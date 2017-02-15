package com.cylan.jiafeigou.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 17-2-4.
 */

public class LoginButtonV2 extends TextView {
    private static final String TAG = "LoginButton";
    private float radiusHeight, markLineWidth;
    private float finalMarkLineWidth;
    private Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path mLinePath = new Path();
    private RectF rectF = new RectF();
    private int animatingState = 3;
    private int rotateDegree;
    private float finalTextSize;
    private float pointRadius = 5;
    private int pointColor = Color.RED;

    public LoginButtonV2(Context context) {
        this(context, null);
    }

    public LoginButtonV2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoginButtonV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray =
                getContext().obtainStyledAttributes(attrs, R.styleable.LoginButtonV2, defStyleAttr, 0);
        pointRadius = typedArray.getFloat(R.styleable.LoginButtonV2_lb_v2_point_radius, 2.0f);
        pointColor = typedArray.getColor(R.styleable.LoginButtonV2_lb_v2_stroke_color, Color.RED);
        float strokeWidth = typedArray.getDimension(R.styleable.LoginButtonV2_lb_v2_stroke_width, 3.0f);
        typedArray.recycle();
        mLinePaint.setColor(pointColor);
        mDotPaint.setColor(pointColor);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG, "onMeasure");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (pointRadius == 0 || pointRadius > h / 2)
            throw new IllegalArgumentException("you must initialize padding");
        Log.d(TAG, "onSizeChanged");
        radiusHeight = (h - 2 * pointRadius) / 2;
        finalMarkLineWidth = markLineWidth = w - pointRadius * 2;
        finalTextSize = px2dip(getTextSize());
        Log.d(TAG, "finalTextSize:" + finalTextSize);
        Log.d(TAG, "getPaddingLeft:" + pointRadius);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(float pxValue) {
        final float scale =
                Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (finalMarkLineWidth < radiusHeight * 2)
            return;
        if ((int) markLineWidth == (int) (radiusHeight * 2)) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, radiusHeight, mLinePaint);
            drawDot(canvas);
            return;
        }
        mLinePath.reset();
        mLinePath.moveTo(getWidth() / 2 - markLineWidth / 2 + radiusHeight, pointRadius);
        mLinePath.lineTo(getWidth() / 2 + (markLineWidth - 2 * radiusHeight) / 2, pointRadius);
        rectF.left = getWidth() / 2 + (markLineWidth - 2 * radiusHeight) / 2 - radiusHeight;
        rectF.top = pointRadius;
        rectF.right = getWidth() / 2 + markLineWidth / 2;
        rectF.bottom = getHeight() - pointRadius;
        mLinePath.arcTo(rectF, -90, 180, true);
        mLinePath.lineTo(getWidth() / 2 - markLineWidth / 2 + radiusHeight, getHeight() - pointRadius);
        rectF.left = getWidth() / 2 - markLineWidth / 2;
        rectF.top = pointRadius;
        rectF.right = getWidth() / 2 - markLineWidth / 2 + 2 * radiusHeight;
        rectF.bottom = getHeight() - pointRadius;
        mLinePath.arcTo(rectF, 90, 180, true);
        canvas.drawPath(mLinePath, mLinePaint);
    }

    private void drawDot(Canvas canvas) {
        final float pointAngle = rotateDegree - (360 - 30) / 2;
        final float x = getMeasuredWidth() / 2
                + radiusHeight * (float) Math.cos(pointAngle * Math.PI / 180);
        final float y = getMeasuredHeight() / 2
                - radiusHeight * (float) Math.sin((pointAngle + 180.0f) * Math.PI / 180);
        mDotPaint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, 2 * pointRadius, mDotPaint);
        mDotPaint.setColor(pointColor);
        canvas.drawCircle(x, y, pointRadius, mDotPaint);
    }

    private ValueAnimator valueAnimatorIn;
    private ValueAnimator valueAnimatorOut;
    private ValueAnimator valueAnimatorRotate;

    private void initRotateAnimator() {
        if (valueAnimatorRotate != null && valueAnimatorRotate.isRunning())
            valueAnimatorRotate.cancel();
        if (valueAnimatorRotate == null) {
            valueAnimatorRotate = ValueAnimator.ofInt(0, 360);
            valueAnimatorRotate.setDuration(800);
            valueAnimatorRotate.setInterpolator(new LinearInterpolator());
            valueAnimatorRotate.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimatorRotate.addUpdateListener((ValueAnimator animation) -> {
                rotateDegree = (int) animation.getAnimatedValue();
                postInvalidate();
            });
        }
        valueAnimatorRotate.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                animatingState = 6;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animatingState = 7;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animatingState = 0;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                animatingState = 8;
            }
        });
        valueAnimatorRotate.start();
    }

    public void initZoomout() {
        if (valueAnimatorOut != null && valueAnimatorOut.isRunning())
            valueAnimatorOut.cancel();
        if (valueAnimatorOut == null) {
            valueAnimatorOut = ValueAnimator.ofFloat(radiusHeight * 2, finalMarkLineWidth);
            valueAnimatorOut.setDuration(500);
            valueAnimatorOut.setInterpolator(new DecelerateInterpolator());
            valueAnimatorOut.addUpdateListener((ValueAnimator animation) -> {
                markLineWidth = (float) animation.getAnimatedValue();
                setTextSize(((markLineWidth - radiusHeight * 2) / (finalMarkLineWidth - radiusHeight * 2)) * finalTextSize);
                postInvalidate();
            });
        }
        valueAnimatorOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                animatingState = 2;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animatingState = 3;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animatingState = 0;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                animatingState = 1;
            }
        });
        valueAnimatorOut.start();
    }

    public void initZoomIn() {
        if (valueAnimatorIn != null && valueAnimatorIn.isRunning())
            valueAnimatorIn.cancel();
        if (valueAnimatorIn == null) {
            valueAnimatorIn = ValueAnimator.ofFloat(finalMarkLineWidth, radiusHeight * 2);
            valueAnimatorIn.setDuration(500);
            valueAnimatorIn.setInterpolator(new DecelerateInterpolator());
            valueAnimatorIn.addUpdateListener((ValueAnimator animation) -> {
                markLineWidth = (float) animation.getAnimatedValue();
                setTextSize((1.0f - (finalMarkLineWidth - markLineWidth) / (finalMarkLineWidth - radiusHeight * 2)) * finalTextSize);
                postInvalidate();
            });
        }
        valueAnimatorIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                animatingState = 4;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animatingState = 5;
                initRotateAnimator();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animatingState = 0;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                animatingState = 1;
            }
        });
        valueAnimatorIn.start();
    }

    public void autoZoom() {
        if (animatingState == 1 || animatingState == 2 || animatingState == 4) return;
        if (animatingState == 3) {
            initZoomIn();
            return;
        }
        if (animatingState == 5) {
            initZoomout();
        }
    }

    public void reverse() {
        if (animatingState == 5) return;
        if (animatingState == 6 || animatingState == 8) {
            if (valueAnimatorRotate != null && valueAnimatorRotate.isRunning()) {
                valueAnimatorRotate.cancel();
                rotateDegree = 0;
            }
            initZoomout();
        }
    }
}
