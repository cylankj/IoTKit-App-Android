package com.cylan.jiafeigou.support.zscan.core;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 16-7-7.
 */
public class LaserView extends View {

    /**
     * 改变线条高度的动画
     */
    private ValueAnimator scanAnimation;

    private Paint linearGradientPaint = new Paint();
    private Paint linePaint = new Paint();
    /**
     * 底部线条颜色
     */
    private int effectStartColor = Color.WHITE;
    private int effectEndColor = Color.WHITE;
    private float lineWidth;
    private int linePositionY = 0;
    private int animationDuration = 400;

    public LaserView(Context context) {
        this(context, null);
    }

    public LaserView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LaserView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScanEffectViewStyle, defStyleAttr, 0);
        effectStartColor = typedArray.getColor(R.styleable.ScanEffectViewStyle_effectStartColor, Color.parseColor("#ff49b8FF"));
        lineWidth = typedArray.getDimension(R.styleable.ScanEffectViewStyle_laserWidth, 5.0f);
        animationDuration = typedArray.getInt(R.styleable.ScanEffectViewStyle_duration, 2500);
        typedArray.recycle();
        init();
    }


    private void init() {
        linePaint = new Paint();
        linePaint.setColor(effectStartColor);
        linePaint.setAntiAlias(true);
    }

    public static int convertToPx(int dp, Resources resources) {
        DisplayMetrics dm = resources.getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    private void createShader() {
        Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredWidth(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        LinearGradient linearGradient = new LinearGradient(getMeasuredWidth() / 2,
                getMeasuredHeight(), getMeasuredWidth() / 2,
                0, effectStartColor, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(linearGradient);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
        // use the bitmap to create the shader
        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
        linearGradientPaint.setShader(bitmapShader);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createShader();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, getTop(), getMeasuredWidth(), linePositionY, linearGradientPaint);
        canvas.drawRect(0, linePositionY, getMeasuredWidth(), linePositionY + lineWidth, linePaint);
    }


    /**
     * 开始动画
     */
    public void startAnimation() {
        if (scanAnimation == null) {
            scanAnimation = ValueAnimator.ofInt(0, getMeasuredHeight());
        }
        if (scanAnimation.isRunning()) {
            return;
        }
        scanAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (animation != null && (animation.getAnimatedValue() instanceof Integer)) {
                    linePositionY = (int) animation.getAnimatedValue();
                    invalidate();
                }
            }
        });
        scanAnimation.setDuration(animationDuration);
        scanAnimation.setInterpolator(new LinearInterpolator());
        scanAnimation.setRepeatCount(ValueAnimator.INFINITE);
        scanAnimation.start();
    }

    /**
     * 停止动画
     */
    public void stopAnimation() {
        if (scanAnimation != null && scanAnimation.isRunning()) {
            scanAnimation.cancel();
        }
    }
}
