package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yanzhendong on 2017/3/20.
 */

public class CountDownView extends View {
    private int width;
    private int height;
    private int maxValue;
    private int progressValue;
    private Paint linePaint;

    public CountDownView(Context context) {
        this(context, null);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        linePaint = new Paint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setMax(int max) {
        this.maxValue = max;
        invalidate();

    }

    public void setProgress(int progress) {
        this.progressValue = progress;
        invalidate();
    }
}
