package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by cylan-hunt on 16-7-1.
 */
public class MsgTextView extends TextView {
    public MsgTextView(Context context) {
        this(context, null);
    }

    public MsgTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MsgTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);
    }

    private String text;
    private float textSize = 5;
    private Paint paint = new Paint();


    public void setTextSize(float textSize) {
        this.textSize = textSize;
        paint.setTextSize(textSize);
        invalidate();
    }

    private float calculateTextWidth() {
        text = getText().toString();
        if (text.length() == 0)
            return 0;
        return paint.measureText(text, 0, text.length());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final float radius = calculateTextWidth() * 2;
        canvas.drawCircle(getMeasuredHeight() / 2, getMeasuredHeight() / 2, radius, paint);
    }
}
