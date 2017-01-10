package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 作者：zsl
 * 创建时间：2016/9/28
 * 描述：
 */
public class CloudLiveVoiceTalkView extends View {

    private boolean status = true;//控件的状态

    private Context context;

    private float longLineLen;
    private float shortLineLen;

    public CloudLiveVoiceTalkView(Context context) {
        this(context, null);
    }

    public CloudLiveVoiceTalkView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CloudLiveVoiceTalkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context);
        initHeigt();
    }

    private void initHeigt() {
        longLineLen = dip2px(context, 26);
        shortLineLen = dip2px(context, 14);
    }

    Paint paint = new Paint();

    private void init(Context context) {
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dip2px(context, 3));
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * 这里是为了实现点击一下控件就停止
     * 控制控件是否跳动  true为跳动，false为停止
     *
     * @param b
     */
    public void change_Status(boolean b) {
        status = b;
    }

    /**
     * 这里是为了实现点击一下控件就停止
     * 拿到控件的状态
     *
     * @return
     */
    public boolean get_Status() {
        return status;
    }

    private void setRect_t(int value1, int value2) {
        longLineLen = dip2px(context, value1);
        shortLineLen = dip2px(context, value2);
    }

    public void reFreshUpView(int longValue, int shortValue) {
        setRect_t(longValue, shortValue);
        if (status == true) {
            CloudLiveVoiceTalkView.this.invalidate();
        } else {

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < 7; i++) {
            float startY = i % 2 != 0 ? getHeight() / 2 - longLineLen / 2 : getHeight() / 2 - shortLineLen / 2;
            float endY = i % 2 != 0 ? getHeight() / 2 + longLineLen / 2 : getHeight() / 2 + shortLineLen / 2;
            canvas.drawLine((18 * i) + getWidth() / 4, startY, (18 * i) + getWidth() / 4, endY, paint);
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
