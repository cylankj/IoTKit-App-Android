package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.cylan.jiafeigou.R;


/**
 * Created by hunt on 15-5-19.
 * This file aims to ....
 */
public class ImageViewTip extends AppCompatImageView {

    private float mDotRadius = 8;
    private boolean showDot = false;
    private Paint mPointPaint = new Paint();
    private Paint borderPaint = new Paint(Color.WHITE);
    private boolean enableBorder = false;
    private int borderColor = Color.WHITE;
    private int pointColor = Color.BLACK;
    private int borderWidth = 0;
    /**
     * 圆形image,bitmap
     */
    private boolean isRoundImage = false;

    public ImageViewTip(Context context) {
        super(context);
    }

    public ImageViewTip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewTip(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageViewTipsTheme, defStyle, 0);
        this.mDotRadius = a.getDimensionPixelSize(R.styleable.ImageViewTipsTheme_t_radius, 5);
        this.showDot = a.getBoolean(R.styleable.ImageViewTipsTheme_t_show_point, false);
        this.borderColor = a.getColor(R.styleable.ImageViewTipsTheme_t_border_color, Color.WHITE);
        borderPaint.setColor(borderColor);
        this.pointColor = a.getColor(R.styleable.ImageViewTipsTheme_t_point_color, Color.WHITE);
        mPointPaint.setColor(pointColor);
        this.borderWidth = a.getDimensionPixelSize(R.styleable.ImageViewTipsTheme_t_border_width, 0);
        this.enableBorder = a.getBoolean(R.styleable.ImageViewTipsTheme_t_enable_border, false);
        this.isRoundImage = a.getBoolean(R.styleable.ImageViewTipsTheme_t_round_image, false);
        a.recycle();
        init();
    }

    public void setPointColor(int pointColor) {
        this.pointColor = pointColor;
    }

    public void enableBoarder(boolean enable) {
        enableBorder = enable;
        invalidate();
    }


    public void setBorderColor(int color) {
        borderColor = color;
        borderPaint.setColor(color);
        invalidate();
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        borderPaint.setStrokeWidth(borderWidth);
        invalidate();
    }

    public void setShowDot(boolean showDot) {
        this.showDot = showDot;
        invalidate();
    }

    public boolean isShowDot() {
        return showDot;
    }

    public void setDotRadius(float mDotRadius) {
        this.mDotRadius = mDotRadius;
        invalidate();
    }

    public float getDotRadius() {
        return mDotRadius;
    }

    private void init() {
        mPointPaint.setAntiAlias(true);
        borderPaint.setAntiAlias(true);
    }

    private RectF redPointRectF = new RectF();

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeRedPointRectF();
    }

    private void computeRedPointRectF() {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            double width = Math.cos(Math.PI / 4) * getWidth() / 2;
            redPointRectF.left = (float) (getWidth() / 2 + width);//中心点
            redPointRectF.top = (float) (getHeight() / 2 - width);//中心点
            //减去 size
            redPointRectF.left = redPointRectF.left - mDotRadius - borderWidth;
            redPointRectF.top = redPointRectF.top - mDotRadius - borderWidth;
            redPointRectF.right = redPointRectF.left + mDotRadius * 2 + borderWidth * 2;
            redPointRectF.bottom = redPointRectF.top + mDotRadius * 2 + borderWidth * 2;
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (!isShowDot())
            return;
        if (enableBorder) {
            canvas.drawCircle(redPointRectF.centerX(), redPointRectF.centerY(), getDotRadius() + borderWidth, borderPaint);
        }
        canvas.drawCircle(redPointRectF.centerX(), redPointRectF.centerY(), getDotRadius(), mPointPaint);
    }

    /**
     * 方法重载，
     *
     * @param drawable
     * @param showDot
     */
    public void setImageDrawable(Drawable drawable, boolean showDot) {
        super.setImageDrawable(drawable);
        this.showDot = showDot;
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        computeRedPointRectF();
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(background);
        computeRedPointRectF();
    }
}
