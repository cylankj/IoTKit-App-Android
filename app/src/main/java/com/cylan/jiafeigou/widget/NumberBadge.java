package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.cylan.jiafeigou.R;


/**
 * Created by hds on 17-6-7.
 */

public class NumberBadge extends AppCompatTextView {

    private Paint bgPaint;
    private Paint pointPaint;

    private Rect defaultTextBounds = new Rect();

    private int drawFlag;
    private float pointRadius;
    private static final String PLACEHOLDER = "0";

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
        int pointBgColor = array.getColor(R.styleable.NumberBadgeStyle_point_bg_color, Color.RED);
        pointRadius = array.getDimension(R.styleable.NumberBadgeStyle_point_radius, 4);
        array.recycle();
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(bgColor);
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(pointBgColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //根据字体大小
        CharSequence content = TextUtils.isEmpty(getText()) ? PLACEHOLDER : getText();
        TextPaint paint = getPaint();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (paint != null) {
            getPaint().getTextBounds(content.toString(), 0, content.length(), defaultTextBounds);
            int w = Math.max(defaultTextBounds.height(), defaultTextBounds.width());
            //需要取外圆
            width = 2 * ((int) (w / 2 / Math.cos(Math.PI / 4)));
        }
        setMeasuredDimension(width, width);//等比
    }

    public void showNumber(int number) {
        if (number <= 0) {
            drawFlag = 0;
            setText("");
            return;
        }
        drawFlag = 2;
        if (number > 99) setText("99+");
        else setText(number + "");
    }

    public void showRedPoint(boolean show) {
        if (show) {
            drawFlag = 1;
            setText("");
            invalidate();
        } else dismiss();
    }

    private void dismiss() {
        drawFlag = 0;
        setText("");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawFlag == 2)
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, bgPaint);
        else if (drawFlag == 1)
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, pointRadius, pointPaint);
        super.onDraw(canvas);
    }
}
