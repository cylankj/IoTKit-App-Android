package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 16-7-1.
 */
public class MsgBoxView extends AppCompatImageView {

    private int textColor;
    private int circleColor;
    private String text;
    private float textSize = 5;
    private static final String[] PLACE_HOLDER = {"0", "00", "000"};
    private Paint circlePaint = new Paint();
    private Paint textPaint = new Paint();
    private int number = 0;
    private Rect textRect = new Rect();

    public MsgBoxView(Context context) {
        this(context, null);
    }

    public MsgBoxView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MsgBoxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.msg_box_style, defStyleAttr, 0);
        circleColor = a.getColor(R.styleable.msg_box_style_mb_circleColor, Color.RED);
        textColor = a.getColor(R.styleable.msg_box_style_mb_textColor, Color.WHITE);
        textSize = a.getDimensionPixelSize(R.styleable.msg_box_style_mb_textSize, 15);
        a.recycle();
        initPaint();
    }

    private void initPaint() {
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(circleColor);

        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private float calculateTextWidth() {
        // 转载请注明出处：http://blog.csdn.net/hursing
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
        if (text == null || text.length() == 0) {
            return 0;
        }
        if (text.length() == 1) {
            textPaint.getTextBounds(PLACE_HOLDER[1], 0, PLACE_HOLDER[1].length(), textRect);
        } else {
            textPaint.getTextBounds(text, 0, text.length(), textRect);
        }
        return textRect.width() + 5;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final float radius = calculateTextWidth() / 2.0f;
        if (radius == 0 || TextUtils.isEmpty(text) || textRect == null) {
            return;
        }
        final int p = getPaddingBottom();
        float x = 0, y = 0;
        if (p >= radius) {
            x = getWidth() - getPaddingEnd();
            y = getPaddingTop();
        } else {
            x = getWidth() - radius;
            y = radius;
        }
        canvas.drawCircle(x, y, radius, circlePaint);
        prepareRect(x, y, radius);
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
        int baseline = (textRect.bottom + textRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(text, textRect.centerX(), baseline, textPaint);
    }

    private void prepareRect(float x, float y, float radius) {
        textRect.bottom = (int) (y + radius);
        textRect.top = (int) (y - radius);
        textRect.left = (int) (x - radius);
        textRect.right = (int) (x + radius);
    }

    public void setText(String text) {
        this.text = text;
        invalidate();
    }

    public void setNumber(int number) {
        if (this.number == 0 && number == 0) {
            setText(null);
        }

    }
}
