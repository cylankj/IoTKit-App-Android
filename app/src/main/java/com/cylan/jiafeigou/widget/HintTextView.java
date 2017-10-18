package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cylan.jiafeigou.R;


/**
 * Created by hds on 17-6-14.
 */

public class HintTextView extends TextView {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 红点半径
     */
    private float defaultRadius;

    private boolean show = false;
    private int position = 1;
    private Rect rect = new Rect();

    public HintTextView(Context context) {
        this(context, null);
    }

    public HintTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HintTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HintTextView, defStyleAttr, 0);
        int pointColor = typedArray.getColor(R.styleable.HintTextView_ht_color, Color.RED);
        defaultRadius = typedArray.getDimension(R.styleable.HintTextView_ht_radius, 8);
        mPaint.setColor(pointColor);
        position = typedArray.getInt(R.styleable.HintTextView_ht_position, 0);
        show = typedArray.getBoolean(R.styleable.HintTextView_ht_show, false);
        typedArray.recycle();
    }

    public void setDefaultRadius(float radius) {
        this.defaultRadius = radius;
        invalidate();
    }

    public void showHint(boolean show) {
        this.show = show;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!show) {
            return;
        }
        Layout layout = getLayout();
        //得到TextView显示有多少行
        int count = getLineCount();

        float x_start, x_stop, x_diff;
        int firstCharInLine, lastCharInLine;
        float x_max = 0, y_max = Integer.MAX_VALUE, y_center;
        float firstLineTop = -1;
        for (int i = 0; i < count; i++) {
            //getLineBounds得到这一行的外包矩形,
            // 这个字符的顶部Y坐标就是rect的top 底部Y坐标就是rect的bottom
            getLineBounds(i, rect);
            if (firstLineTop == -1) {
                firstLineTop = rect.top;
            }
            firstCharInLine = layout.getLineStart(i);
            lastCharInLine = layout.getLineEnd(i);

            //要得到这个字符的左边X坐标 用layout.getPrimaryHorizontal
            //得到字符的右边X坐标用layout.getSecondaryHorizontal
            x_start = layout.getPrimaryHorizontal(firstCharInLine);
            x_diff = layout.getPrimaryHorizontal(firstCharInLine + 1) - x_start;
            x_stop = layout.getPrimaryHorizontal(lastCharInLine - 1) + x_diff;
//            canvas.drawLine(x_start, baseline + 5, x_stop, baseline + 5, mPaint);
            if (x_max <= x_stop) {
                x_max = x_stop;
            }
            if (y_max >= rect.top) {
                y_max = rect.top;
            }
        }
        canvas.drawCircle(position == 0 ? x_max + defaultRadius + getPaddingLeft() :
                        x_max + defaultRadius * 2 + getPaddingLeft(),
                position == 0 ? y_max + defaultRadius : getHeight() / 2,
                defaultRadius, mPaint);
    }
}
