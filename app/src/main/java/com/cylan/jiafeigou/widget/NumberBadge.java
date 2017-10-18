package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.cylan.jiafeigou.R;


/**
 * Created by hds on 17-6-7.
 */

public class NumberBadge extends AppCompatTextView {

    private Paint bgPaint;

    private int drawFlag;
    private float pointRadius;

    public NumberBadge(Context context) {
        this(context, null);
    }

    public NumberBadge(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberBadge(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.NumberBadgeStyle, defStyleAttr, 0);
        int bgColor = array.getColor(R.styleable.NumberBadgeStyle_bg_color, Color.RED);
        pointRadius = array.getDimension(R.styleable.NumberBadgeStyle_point_radius, 4);
        array.recycle();
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(bgColor);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int h = this.getMeasuredHeight();
        int w = this.getMeasuredWidth();
        int r = Math.max(w, h);
        setMeasuredDimension(r, r);
    }


    /**
     * 小于0:隐藏,等于零:红点,大于零:数字
     *
     * @param number{}
     */
    public void showNumber(int number) {
        drawFlag = 2;
        if (number > 99) {
            setText(" 99+ ");
        } else {
            setText(number + "");
        }
    }

    public void showHint(boolean show) {
        drawFlag = show ? 1 : 0;
        setText("");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int h = this.getHeight();
        int w = this.getWidth();
        int diameter = Math.max(h, w);
        int radius = diameter / 2;
        if (drawFlag == 2) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, bgPaint);
        } else if (drawFlag == 1) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, pointRadius, bgPaint);
        }
        super.onDraw(canvas);
    }
}
