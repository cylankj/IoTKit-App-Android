package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yzd on 16-12-19.
 */

public class TimerView extends View {
    private float mCX;
    private float mCY;
    private float mRadius;
    private Paint mBackgroundPaint;
    private float mStartAngle;
    private float mSweepAngle;
    private Paint mNormalPaint;
    private RectF mArcOval;
    private STATE mState;
    private float mRestoreRadius;
    private RectF mRecordRect;
    private float mRecordRadius;

    public TimerView(Context context) {
        this(context, null);
    }

    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public enum STATE {
        RESTORE, RECORD
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCX = w / 2;
        mCY = h / 2;

    }

    private void init() {
        mBackgroundPaint = new TextPaint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setColor(Color.parseColor("#FFFFFF"));
        mBackgroundPaint.setAlpha((int) (255 * 0.34f));
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mNormalPaint = new TextPaint(mBackgroundPaint);
        mNormalPaint.setAlpha(255);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //先画外圈背景圆
        canvas.drawCircle(mCX, mCY, mRadius, mBackgroundPaint);
        //再画外圈进度
        canvas.drawArc(mArcOval, mStartAngle, mSweepAngle, false, mNormalPaint);
        //再根据当前state画内圈圆
        if (mState == STATE.RESTORE) {
            canvas.drawCircle(mCX, mCY, mRestoreRadius, mNormalPaint);
        } else {
            canvas.drawRoundRect(mRecordRect, mRecordRadius, mRecordRadius, mNormalPaint);
        }
        if (mState == STATE.RECORD) postDelayed(this::invalidate, 200);
    }

    public void setMaxTime(int time) {

    }

    public void setRecordTime(int time) {

    }

    public void startRecord() {

    }

    public void restoreRecord() {

    }

    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
