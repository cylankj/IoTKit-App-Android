package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.cylan.jiafeigou.R;

/**
 * 广告页面倒计时
 * Created by yanzhendong on 2017/3/20.
 */

public class AdsTimerView extends AppCompatTextView {

    /**
     * 时长
     */
    private int maxTime;
    private Paint progressPaint;
    private Paint bgPaint;
    private int bgColor;
    private int progressColor;
    private float progressWidth;
    private int currentProgress = 300;

    private RectF circleRect = new RectF();

    public AdsTimerView(Context context) {
        this(context, null);
    }

    public AdsTimerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdsTimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AdsTimerStyle, defStyleAttr, 0);
        bgColor = a.getColor(R.styleable.AdsTimerStyle_a_bg_color, Color.BLACK);
        progressColor = a.getColor(R.styleable.AdsTimerStyle_a_progress_color, Color.WHITE);
        progressWidth = a.getDimension(R.styleable.AdsTimerStyle_a_progress_width, 3);
        maxTime = a.getInt(R.styleable.AdsTimerStyle_a_max_time, 3);
//        defaultString = a.getString(R.styleable.AdsTimerStyle_a_default_content);
        a.recycle();
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeWidth(progressWidth);
        progressPaint.setStyle(Paint.Style.STROKE);
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(bgColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        circleRect.left = progressWidth / 2;
        circleRect.top = progressWidth / 2;
        circleRect.right = getWidth() - progressWidth / 2;
        circleRect.bottom = getHeight() - progressWidth / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //根据字体大小
        CharSequence content = getText();
        if (content == null || content.length() == 0) {
            throw new IllegalArgumentException("清填充文字");
        }
        TextPaint paint = getPaint();
        if (paint != null) {
            float w = paint.measureText(content, 0, content.length());
            //需要取外圆
            width = 2 * ((int) (w / 2 / Math.cos(Math.PI / 4)));
        }
        setMeasuredDimension(width, width);//等比
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //先画背景
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, bgPaint);
        super.onDraw(canvas);
        canvas.drawArc(circleRect, -90, currentProgress, false, progressPaint);
        if (currentProgress < 360) {
            postDelayed(runnable, 20);
        } else {
            if (boomer != null) {
                boomer.onTimerFinish();
            }
        }
    }

    /**
     * 定期刷新
     */
    private Runnable runnable = () -> {
        currentProgress += 3;
        invalidate();
    };

    public void startTimer() {
        this.currentProgress = 0;
        invalidate();
    }

    private Boomer boomer;

    public void setBoomer(Boomer boomer) {
        this.boomer = boomer;
    }

    public interface Boomer {
        void onTimerFinish();
    }
}
