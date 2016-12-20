package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.cylan.jiafeigou.R;

/**
 * Created by yzd on 16-12-19.
 */

public class RecordControllerView extends View {
    private float mCX;
    private float mCY;
    private float mRadius;
    private Paint mBackgroundPaint;
    private float mStartAngle;
    private float mSweepAngle;
    private Paint mNormalPaint;
    private RectF mArcOval;
    private STATE mState = STATE.RESTORE;
    private float mRestoreRadius;
    private RectF mRecordRect;
    private float mRecordRadius;

    private boolean mIsRecording = false;
    private float mDegree;
    private long mMaxTime;
    private long mRecordTime;
    private static final int UPDATE_TIME_INTERVAL = 1000 * 20;//每20秒更新一次,因为不显示秒,所以可以慢一点更新

    public RecordControllerView(Context context) {
        this(context, null);
    }

    public RecordControllerView(Context context, AttributeSet attrs) {
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
        mArcOval = new RectF(3, 3, w - 3, h - 3);
    }

    private void init() {
        mNormalPaint = new TextPaint();
        mNormalPaint.setStyle(Paint.Style.STROKE);
        mNormalPaint.setColor(Color.WHITE);
        mNormalPaint.setStrokeWidth(5);
        mNormalPaint.setAntiAlias(true);
        mStartAngle = -90;
        mSweepAngle = 90;
        setBackgroundResource(R.drawable.delay_icon_play);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mIsRecording) canvas.drawArc(mArcOval, -90, mDegree, false, mNormalPaint);
    }

    public void setMaxTime(long time) {
        mMaxTime = time;
    }

    public void setRecordTime(long time) {
        mRecordTime = time;
        mDegree = mRecordTime / mMaxTime * 360F;
        invalidate();
    }

    public void startRecord() {
        if (mMaxTime > mRecordTime) {
            mIsRecording = true;
            setBackgroundResource(R.drawable.delay_icon_pause);
            demo();
        }
    }

    public void restoreRecord() {
        mIsRecording = false;
        setBackgroundResource(R.drawable.delay_icon_play);
    }

    public void demo() {
        mDegree += (360 / 30);
        invalidate();
        if (mDegree < 360) postDelayed(this::demo, (1000 / 30));
    }


    private void updateRecordTime(int inc) {

        if (inc == -1 && mDegree < 360) {
            mDegree += (360 / 30);
            invalidate();

        } else if (inc == -1 && mDegree > 360) {
            mDegree = 0;
            updateRecordTime(UPDATE_TIME_INTERVAL);
        } else {
            postDelayed(() -> {
                mRecordTime += inc;
                mDegree = ((float) mRecordTime) / mMaxTime * 360;
                invalidate();
                if (mIsRecording) updateRecordTime(UPDATE_TIME_INTERVAL);
            }, UPDATE_TIME_INTERVAL);
        }
    }


    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public boolean isRecording() {
        return mIsRecording;
    }

}
