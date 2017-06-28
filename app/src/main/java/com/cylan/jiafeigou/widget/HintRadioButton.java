package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.util.Log;

import com.cylan.jiafeigou.R;

/**
 * Created by hds on 17-6-28.
 */

public class HintRadioButton extends AppCompatRadioButton {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect rect = new Rect();
    /**
     * 红点半径
     */
    private float defaultRadius;
    /**
     * 红点padding
     */
    private float paddingTop;
    private boolean show = false;

    public HintRadioButton(Context context) {
        this(context, null);
    }

    public HintRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HintRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs,
                R.styleable.HintRadioButton, defStyleAttr, 0);
        int pointColor = typedArray.getColor(R.styleable.HintRadioButton_cb_color, Color.RED);
        defaultRadius = typedArray.getDimension(R.styleable.HintRadioButton_cb_radius, 5);
        paddingTop = typedArray.getDimension(R.styleable.HintRadioButton_cb_padding_top, 0);
        mPaint.setColor(pointColor);
        typedArray.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Drawable[] drawables = getCompoundDrawables();
        for (Drawable d : drawables) {
            if (d == null) continue;
            Log.d("onDraw", "onSizeChanged: " + (rect = d.getBounds()));
        }
        Log.d("onDraw", "bg: " + getBackground());
        drawables = getCompoundDrawablesRelative();
        for (Drawable d : drawables) {
            if (d == null) continue;
            Log.d("onDraw", "re: " + (rect = d.getBounds()));
        }
    }


    public void showRedHint(boolean show) {
        this.show = show;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!show) return;
        canvas.drawCircle(getMeasuredWidth() / 2 + rect.width() / 2,
                defaultRadius + paddingTop,
                defaultRadius,
                mPaint);
    }
}

