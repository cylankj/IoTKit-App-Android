package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.cylan.jiafeigou.R;

/**
 * Created by hds on 17-6-28.
 */

public class HintTextView extends AppCompatTextView {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 红点半径
     */
    private float defaultRadius;
    /**
     * 红点padding
     */
    private float padding;
    private boolean show = false;

    public HintTextView(Context context) {
        this(context, null);
    }

    public HintTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HintTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray =
                getContext().obtainStyledAttributes(attrs, R.styleable.HintTextView, defStyleAttr, 0);
        int pointColor = typedArray.getColor(R.styleable.HintTextView_ht_color, Color.RED);
        defaultRadius = typedArray.getDimension(R.styleable.HintTextView_ht_radius, 5);
        padding = typedArray.getDimension(R.styleable.HintTextView_ht_padding, 5);
        mPaint.setColor(pointColor);
        typedArray.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }


    public void showRedHint(boolean show) {
        this.show = show;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!show) return;
        canvas.drawCircle(getMeasuredWidth() - padding,
                defaultRadius + padding,
                defaultRadius,
                mPaint);
    }

}
