package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;


/**
 * Created by hunt on 15-5-19.
 * This file aims to ....
 */
public class ImageViewTip extends ImageView {

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
    /**
     * 总共有8个位置,“米”各个角，左上角为0,顺时针增加。
     */

    /**
     * 0-1-2
     * 7---3
     * 6-5-4
     */
    private int position = 0;

    private boolean ignorePadding = false;


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
//        setDotRadius(radius);
        this.showDot = a.getBoolean(R.styleable.ImageViewTipsTheme_t_show_point, false);
//        setShowDot(show);
        this.ignorePadding = a.getBoolean(R.styleable.ImageViewTipsTheme_t_ignore, false);
//        setIgnorePadding(ignore);
        this.position = a.getInteger(R.styleable.ImageViewTipsTheme_t_position, 2);
//        setPosition(position);

        this.borderColor = a.getColor(R.styleable.ImageViewTipsTheme_t_border_color, Color.WHITE);
        borderPaint.setColor(borderColor);
//        setBorderColor(borderColor);
        this.pointColor = a.getColor(R.styleable.ImageViewTipsTheme_t_point_color, Color.WHITE);
        mPointPaint.setColor(pointColor);
        this.borderWidth = a.getDimensionPixelSize(R.styleable.ImageViewTipsTheme_t_border_width, 0);
//        setBorderWidth(borderWidth);

        this.enableBorder = a.getBoolean(R.styleable.ImageViewTipsTheme_t_enable_border, false);
//        enableBoarder(enableBorder);
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

    public void setIgnorePadding(boolean ignore) {
        this.ignorePadding = ignore;
        requestLayout();
    }

    public void setPosition(int position) {
        this.position = position;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        x[0] = (ignorePadding ? getPaddingLeft() : 0) + getDotRadius() + borderWidth;
        x[1] = getWidth() / 2;
        x[2] = (ignorePadding ? getWidth() - getDotRadius() - getPaddingRight() : getWidth() - getDotRadius()) - borderWidth;
        y[0] = (ignorePadding ? getDotRadius() + getPaddingTop() : getDotRadius()) + borderWidth;
        y[1] = getHeight() / 2;
        y[2] = (ignorePadding ? getHeight() - getDotRadius() - getPaddingBottom() : getHeight() - getDotRadius()) - borderWidth;
    }

    private float[] x = {0f, 0f, 0f};
    private float[] y = {0f, 0f, 0f};
    private int[][] pos = {
            {0, 0}, {1, 0}, {2, 0},
            {2, 1}, {2, 2},
            {1, 2}, {0, 2}, {0, 1}};

    private PointF getPoint(int position) {
        PointF rectF = new PointF();
        rectF.x = x[pos[position][0]];
        rectF.y = y[pos[position][1]];
        return rectF;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (enableBorder) {
            if (!this.isRoundImage)
                canvas.drawCircle(getPoint(position).x, getPoint(position).y, getDotRadius() + borderWidth, borderPaint);
            else {
                canvas.drawCircle(getRoundPos(position).x, getRoundPos(position).y, getDotRadius() + borderWidth, borderPaint);
            }
        }
        if (isShowDot()) {
            if (!this.isRoundImage)
                canvas.drawCircle(getPoint(position).x, getPoint(position).y, getDotRadius(), mPointPaint);
            else {
                canvas.drawCircle(getRoundPos(position).x, getRoundPos(position).y, getDotRadius(), mPointPaint);
            }
        }
    }

    /**
     * 只有0,2,4,6才有效
     *
     * @param position
     * @return
     */
    private PointF getRoundPos(int position) {
        PointF rectF = new PointF();
        if (position == 1 || position == 3 || position == 5 || position == 7)
            return rectF;
        final float tmp = (float) (getWidth() / 2 - getWidth() / 2 * Math.cos(Math.PI / 4));
        if (position == 0) {
            rectF.x = x[pos[position][0]] + tmp - getDotRadius() - borderWidth;
            rectF.y = y[pos[position][1]] + tmp - getDotRadius() - borderWidth;
        }
        if (position == 2) {
            rectF.x = x[pos[position][0]] - tmp + getDotRadius() + borderWidth;
            rectF.y = y[pos[position][1]] + tmp - getDotRadius() - borderWidth;
        }
        if (position == 4) {
            rectF.x = x[pos[position][0]] - tmp + getDotRadius() + borderWidth;
            rectF.y = y[pos[position][1]] - tmp + getDotRadius() + borderWidth;
        }
        if (position == 6) {
            rectF.x = x[pos[position][0]] + tmp - getDotRadius() - borderWidth;
            rectF.y = y[pos[position][1]] - tmp + getDotRadius() + borderWidth;
        }
        return rectF;
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
}
