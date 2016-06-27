package com.cylan.jiafeigou.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
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
    int strokeWidth;

    public LoginButton(Context context) {
        super(context);
        init(context, null, 0);
    }

    public LoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public LoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }


    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) {
            strokeColor = Color.BLACK;
            strokeWidth = 5;
        } else {
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.LoginButton, defStyleAttr, 0);
            strokeColor = array.getColor(R.styleable.LoginButton_lb_stroke_color, Color.BLACK);
            strokeWidth = 5;  //
        }
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(strokeColor);
        paint.setAntiAlias(true);
        path = new Path();
        text = getText().toString();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!isInit) {
            srcW = w;
            srcH = h;
            isInit = true;
        }
        viewW = w - strokeWidth * 2;
        viewH = h - strokeWidth * 2;
        paint.setTextSize(viewH / 3);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.rotate(degrees, canvas.getWidth() / 2, canvas.getHeight() / 2);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        if (viewH > viewW) {
            return;
        }
        path.reset();
        path.moveTo(viewH / 2 + strokeWidth, strokeWidth);
        path.lineTo(viewW - viewH / 2 + strokeWidth, strokeWidth);
        path.arcTo(new RectF(viewW - viewH + strokeWidth, strokeWidth, viewW + strokeWidth, viewH + strokeWidth), -90, 180, false);
        path.lineTo(viewH / 2 + strokeWidth, viewH + strokeWidth);
        path.arcTo(new RectF(strokeWidth, strokeWidth, viewH + strokeWidth, viewH + strokeWidth), 90, 180, false);
        path.close();
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
        if (viewH == viewW) {
            canvas.save();
            canvas.rotate(30, canvas.getWidth() / 2, canvas.getHeight() / 2);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(viewH / 2, strokeWidth, 25, paint); //圆点外圆白底
            paint.setColor(strokeColor);
            canvas.drawCircle(viewH / 2, strokeWidth, 10, paint);//圆点内圆
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
                int currW = ((Integer) animation.getAnimatedValue()).intValue();
                ViewGroup.LayoutParams params = getLayoutParams();
                params.width = currW + 10;
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
                degrees = ((Integer) animation.getAnimatedValue()).intValue();
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
        valueAnimator3 = ValueAnimator.ofInt(viewW, srcW - 10);
        valueAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currW = ((Integer) animation.getAnimatedValue()).intValue();
                ViewGroup.LayoutParams params = getLayoutParams();
                params.width = currW + 10;
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
