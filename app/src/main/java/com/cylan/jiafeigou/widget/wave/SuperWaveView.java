package com.cylan.jiafeigou.widget.wave;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.cylan.jiafeigou.R;

import java.util.Random;

/**
 * Created by cylan-hunt on 16-7-19.
 */
public class SuperWaveView extends View {

    /**
     * 屏幕宽度像素
     */
    private final int screenWidth;
    /**
     * default 1
     */
    private int waveCount = 1;

    private int waveColor;

    private static final float DEFAULT_AMPLITUDE_RATIO = 1.0f;
    private float amplitudeRatio = 1.0f;
    private Shader waveShader[];

    private Paint wavePaint[];

    private Matrix[] shaderMatrix;

    private WaveAnimation waveAnimation;

    private float waveShiftRatio[];

    public SuperWaveView(Context context) {
        this(context, null);
    }

    public SuperWaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SuperWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SuperWaveTheme, defStyleAttr, 0);
        waveCount = typedArray.getInteger(R.styleable.SuperWaveTheme_waveCount, 3);
        waveColor = typedArray.getColor(R.styleable.SuperWaveTheme_waveColor,
                Color.parseColor("#6CFFFFFF"));
        typedArray.recycle();
        init();
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    }


    private void init() {
        wavePaint = new Paint[waveCount];
        for (int i = 0; i < waveCount; i++) {
            wavePaint[i] = new Paint();
            wavePaint[i].setAntiAlias(true);
            wavePaint[i].setColor(waveColor);
        }
        waveShader = new Shader[waveCount];
        waveShiftRatio = new float[waveCount];
        shaderMatrix = new Matrix[waveCount];
        for (int i = 0; i < waveCount; i++)
            shaderMatrix[i] = new Matrix();
    }


    public void setWaveCount(int waveCount) {
        this.waveCount = waveCount;
    }

    public void setWaveColor(int waveColor) {
        this.waveColor = waveColor;
    }

    private int dp2px(int dp) {
        return (int) (Resources.getSystem().getDisplayMetrics().density * dp + 0.5f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        for (int i = 0; i < waveCount; i++) {
            waveShader[i] = createShader(createSinePath(generatePhase(),
                    generateWaterLevel(i),
                    generateAmplitude()), i);
            wavePaint[i].setShader(waveShader[i]);
        }
    }

    private final float[] phaseArray = {2.0f, 2.0f, 2.0f, 2.0f, 2.0f};

    private final float amplitudeRationArray[] = {0.1f, 0.12f, 0.13f, 0.15f};

    private float generatePhase() {
        return (float) (phaseArray[new Random().nextInt(4)] * Math.PI / getMeasuredWidth());
    }

    private int generateWaterLevel(final int index) {
        return (int) (dp2px(getHeight() * index / 10) + generateAmplitude());
    }


    private float generateAmplitude() {
        return amplitudeRationArray[new Random().nextInt(4)] * getMeasuredHeight();
    }

    private Shader createShader(final Path path, final int index) {
        Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(),
                getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPath(path, wavePaint[index]);
        return new BitmapShader(bitmap,
                Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
    }

    private Path createSinePath(final float phase, final float waterLevel, final float amplitude) {
        Path path = new Path();
        path.moveTo(0, getMeasuredHeight());
        path.lineTo(0, waterLevel);
        for (int xNext = 0; xNext <= getMeasuredWidth(); xNext++) {
            float yNext = (float) (getMeasuredHeight() - waterLevel
                    + amplitude * (float) Math.sin(xNext * phase + phase / 5) + Math.sin(xNext * phase));
            if (yNext < 0) yNext = 0;
            path.lineTo(xNext, yNext);
        }
        path.lineTo(getMeasuredWidth(), getMeasuredHeight());
        path.close();
        return path;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < waveCount; i++)
            drawShader(canvas, i);
    }


    /**
     * Set vertical size of wave according to <code>amplitudeRatio</code>
     *
     * @param amplitudeRatio Default to be 0.05. Result of amplitudeRatio + waterLevelRatio should be less than 1.
     *                       <br/>Ratio of amplitude to height of WaveView.
     */
    public void setAmplitudeRatio(float amplitudeRatio) {
        if (this.amplitudeRatio != amplitudeRatio) {
            this.amplitudeRatio = amplitudeRatio;
            invalidate();
        }
    }

    public float getAmplitudeRatio() {
        return amplitudeRatio;
    }

    private void drawShader(Canvas canvas, final int index) {
        // perform paint shader according to mShowWave state
        if (waveShader != null) {
            // first call after mShowWave, assign it to our paint
            if (wavePaint[index].getShader() == null) {
                wavePaint[index].setShader(waveShader[index]);
            }
            // sacle shader according to mWaveLengthRatio and mAmplitudeRatio
            // this decides the size(mWaveLengthRatio for width, mAmplitudeRatio for height) of waves
            shaderMatrix[index].setScale(
                    1,
                    amplitudeRatio / DEFAULT_AMPLITUDE_RATIO,
                    0,
                    getHeight());
            // translate shader according to mWaveShiftRatio and mWaterLevelRatio
            // this decides the initSubscription position(mWaveShiftRatio for x, mWaterLevelRatio for y) of waves
            shaderMatrix[index].postTranslate(
                    waveShiftRatio[index] * getWidth(),
                    0);
            // assign matrix to invalidate the shader
            waveShader[index].setLocalMatrix(shaderMatrix[index]);
            canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), wavePaint[index]);
        }
    }


    public void startAnimation() {
        if (waveAnimation == null)
            waveAnimation = new WaveAnimation();
        waveAnimation.start();
    }

    public void stopAnimation() {
        if (waveAnimation != null)
            waveAnimation.stop();

    }

    private class WaveAnimation {

        private final long[] duration = {15000, 30000, 35000};
        ValueAnimator[] valueAnimators;

        public WaveAnimation() {
            valueAnimators = new ValueAnimator[waveCount];
            for (int i = 0; i < waveCount; i++)
                initAnimation(i);
        }

        private void start() {
            if (valueAnimators != null) {
                for (ValueAnimator animator : valueAnimators) {
                    if (!animator.isRunning())
                        animator.start();
                }
            }
        }

        private void stop() {
            if (valueAnimators != null) {
                for (ValueAnimator animator : valueAnimators) {
                    if (animator.isRunning())
                        animator.cancel();
                }
            }
        }

        private void initAnimation(final int index) {
            final float startX = index == 1 ? 1.0f : 0.0f;
            final float endX = index == 1 ? 0.0f : 1.0f;
            valueAnimators[index] = ValueAnimator.ofFloat(startX, endX);
            valueAnimators[index].setRepeatCount(ValueAnimator.INFINITE);
            valueAnimators[index].setDuration(duration[index]);
            valueAnimators[index].setInterpolator(new LinearInterpolator());
            valueAnimators[index].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    waveShiftRatio[index] = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
        }
    }
}
