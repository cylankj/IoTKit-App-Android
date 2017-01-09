package com.cylan.jiafeigou.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * @author zhenbin_wei
 * @date 2016/6/17 13:50
 * @Description: ${TODO}(用一句话描述该文件做什么)
 */
public class LoginButton extends TextView {

    private Paint paint;
    private Path path;
    private int viewW;
    private int viewH;
    private String text = "";
    private ValueAnimator valueAnimator;
    private ValueAnimator valueAnimator2;
    private ValueAnimator valueAnimator3;
    private float degrees = 0;

    private int srcW = 0;
    private int srcH = 0;

    private boolean isInit = false;

    private boolean hasRotate = false;

    int strokeColor;

    private PaintFlagsDrawFilter paintFlagsDrawFilter;

    private RectF rectFL = new RectF();

    public LoginButton(Context context) {
        this(context, null);
    }

    public LoginButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private float outerCircleRadius = 5;
    private float innerCircleRadius = 3;
    private float strokeWidth = 1;

    private void init(AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) {
            strokeColor = Color.BLACK;
            strokeWidth = convertToPx(strokeWidth, getResources());
        } else {
            TypedArray array = null;
            try {
                array = getContext().obtainStyledAttributes(attrs, R.styleable.LoginButton, defStyleAttr, 0);
                strokeColor = array.getColor(R.styleable.LoginButton_lb_stroke_color, Color.BLACK);
                strokeWidth = convertToPx(strokeWidth, getResources());
            } finally {
                if (array != null) array.recycle();
            }
        }

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(strokeColor);
        paint.setAntiAlias(true);
        path = new Path();
        text = getText().toString();
        paintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        outerCircleRadius = convertToPx(outerCircleRadius, getResources());
        innerCircleRadius = convertToPx(innerCircleRadius, getResources());
    }

    public static float convertToPx(float dp, Resources resources) {
        DisplayMetrics dm = resources.getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!isInit) {
            srcW = w;
            srcH = h;
            isInit = true;
        }
        viewW = w;
        viewH = h;
        paint.setTextSize(viewH / 3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.rotate(degrees, canvas.getWidth() / 2, canvas.getHeight() / 2);
        canvas.setDrawFilter(paintFlagsDrawFilter);
        if (viewH > viewW) {
            viewW = viewH;
//            return;
        }
        if (viewH != viewW) {
            path.reset();
            path.moveTo(viewH / 2 + strokeWidth / 2, outerCircleRadius);
            path.lineTo(viewW - viewH / 2 + outerCircleRadius / 2, outerCircleRadius);
            rectFL.left = viewW - viewH;
            rectFL.top = outerCircleRadius;
            rectFL.right = viewW - outerCircleRadius / 2 - strokeWidth / 2;
            rectFL.bottom = viewH - outerCircleRadius;
            path.arcTo(rectFL, -90, 180, false);
            path.lineTo(viewH / 2, viewH - outerCircleRadius);
            rectFL.left = outerCircleRadius;
            rectFL.top = outerCircleRadius;
            rectFL.right = viewH;
            rectFL.bottom = viewH - outerCircleRadius;
            path.arcTo(rectFL, 90, 180, false);
            path.close();
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(path, paint);
        }
        if (viewH == viewW) {
            canvas.save();
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(viewH / 2, viewW / 2, viewH / 2 - outerCircleRadius, paint);
            canvas.rotate(30, canvas.getWidth() / 2, canvas.getHeight() / 2);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(viewH / 2, outerCircleRadius, outerCircleRadius, paint); //圆点外圆白底
            paint.setColor(strokeColor);
            canvas.drawCircle(viewH / 2, outerCircleRadius, innerCircleRadius, paint);//圆点内圆
            canvas.restore();
        }
    }


    public void viewZoomSmall() {
        if (viewW < viewH) {
            return;
        }

        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofInt(viewW, viewH);
        }
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currW = (int) animation.getAnimatedValue();
                if (currW < viewH) {
                    currW = viewH;
                }
                ViewGroup.LayoutParams params = getLayoutParams();
                params.width = currW;
                setLayoutParams(params);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setText("");
                viewRotate();
            }
        });
        valueAnimator.setDuration(600);
        if (!valueAnimator.isRunning() && !hasRotate) {
            valueAnimator.start();
        }
    }


    private void viewRotate() {
        hasRotate = true;
        if (valueAnimator2 != null && valueAnimator2.isRunning()) {
            return;
        }
        valueAnimator2 = ValueAnimator.ofInt(0, 360);
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                degrees = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator2.setInterpolator(new LinearInterpolator());
        valueAnimator2.setDuration(800);
        valueAnimator2.setRepeatCount(ValueAnimator.INFINITE);//动画重复次数
        valueAnimator2.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator2.start();
    }


    public void viewZoomBig() {
        degrees = 0;
        if (valueAnimator2 != null) {
            valueAnimator2.cancel();
        }
        if (valueAnimator3 != null && valueAnimator3.isRunning()) {
            return;
        }
        if (viewW != viewH) {
            return;
        }
        valueAnimator3 = ValueAnimator.ofFloat(viewW, srcW - strokeWidth);
        valueAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currW = (float) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = getLayoutParams();
                params.width = (int) (currW + strokeWidth);
                setLayoutParams(params);
            }
        });
        valueAnimator3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setText(text);
                hasRotate = false;
            }
        });
        valueAnimator3.setDuration(600);
        valueAnimator3.start();
    }

    public void cancelAnim() {
        if (valueAnimator2 != null) {
            valueAnimator2.cancel();
        }
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        if (valueAnimator3 != null) {
            valueAnimator3.cancel();
        }
        hasRotate = false;
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = srcW;
        params.height = srcH;
        degrees = 0;
        setLayoutParams(params);
        invalidate();
    }
}
