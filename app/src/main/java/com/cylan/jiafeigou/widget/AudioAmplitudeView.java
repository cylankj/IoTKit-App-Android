package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.DensityUtil;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-12-14
 * Time: 16:06
 */

public class AudioAmplitudeView extends View {

    private Paint mPaint1;
    private Paint mPaint2;
    private int amplitude = 0;
    private boolean isLeft = true;
    private Context mContext;

    public AudioAmplitudeView(Context context) {
        super(context);
        mContext=context;
        init();
    }

    public AudioAmplitudeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioAmplitudeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AudioAmplitudeView);
        isLeft = a.getBoolean(0, true);
        a.recycle();
        init();
    }

    private void init() {
        //深蓝色
        mPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint1.setColor(0xff01adf0);
        //浅蓝色
        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint2.setColor(0xff7dd4f6);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();
        int width = getWidth() - paddingLeft - paddingRight;
        int height = getHeight() - paddingTop - paddingBottom;

        for (int i = 0; i < 7; i++) {
            if (i % 2 == 0) {//短
                int minlen = DensityUtil.dip2px(mContext,15);
                int callen = minlen + DensityUtil.dip2px(mContext,30) * amplitude / 12;
                int len = callen > DensityUtil.dip2px(mContext,27) ? DensityUtil.dip2px(mContext,27) : callen;
                canvas.drawRoundRect(getRectF(len, width, height, i), 5, 5, mPaint2);
            } else { //长
                int minlen = DensityUtil.dip2px(mContext,15);
                int callen = minlen + DensityUtil.dip2px(mContext,30) * amplitude / 12;
                int len = callen > DensityUtil.dip2px(mContext,45) ? DensityUtil.dip2px(mContext,45) : callen;
                canvas.drawRoundRect(getRectF(len, width, height, i), 5, 5, mPaint1);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST
                && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(200, 200);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(200, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, 200);
        }
    }

    public void setAmplitude(int amp) {
        this.amplitude = amp;
        invalidate();
    }

    private RectF getRectF(int len, int width, int heigth, int index) {
        int singleWidth= DensityUtil.dip2px(mContext,5);
        int padding= DensityUtil.dip2px(mContext,6);
        int magin= DensityUtil.dip2px(mContext,20);

        int startLeft;
        if (isLeft) {
            startLeft = width - singleWidth * 7 - padding * 6 - magin;
        } else {
            startLeft = magin;
        }
        RectF r2 = new RectF();
        r2.left = startLeft + index * (singleWidth+padding);
        r2.top = (heigth - len) / 2;
        r2.right = startLeft + index* (singleWidth+padding) + singleWidth;
        r2.bottom = (heigth - len) / 2 + len;
        return r2;
    }


}
