package com.cylan.jiafeigou.support.zscan.core;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.cylan.utils.DensityUtils;


public class ViewFinderView extends View implements IViewFinder {
    private static final String TAG = "ViewFinderView";

    private Rect mFramingRect;
    private Rect mTextRect;


    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;

    private static final float LANDSCAPE_WIDTH_RATIO = 4f / 8;
    private static final float LANDSCAPE_HEIGHT_RATIO = 4f / 8;
    private static final int LANDSCAPE_MAX_FRAME_WIDTH = (int) (1920 * LANDSCAPE_WIDTH_RATIO); // = 5/8 * 1920
    private static final int LANDSCAPE_MAX_FRAME_HEIGHT = (int) (1080 * LANDSCAPE_HEIGHT_RATIO); // = 5/8 * 1080

    private static final float PORTRAIT_WIDTH_RATIO = 6f / 10;
    private static final float PORTRAIT_HEIGHT_RATIO = 3f / 8;
    private static final int PORTRAIT_MAX_FRAME_WIDTH = (int) (1080 * PORTRAIT_WIDTH_RATIO); // = 7/8 * 1080
    private static final int PORTRAIT_MAX_FRAME_HEIGHT = (int) (1920 * PORTRAIT_HEIGHT_RATIO); // = 3/8 * 1920

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private int scannerAlpha;
    private static final int POINT_SIZE = 10;
    private static final long ANIMATION_DELAY = 80l;

    private final int mDefaultLaserColor = Color.BLACK;
    private final int mDefaultMaskColor = Color.parseColor("#80000000");
    private final int mDefaultBorderColor = Color.WHITE;
    private final int mDefaultBorderStrokeWidth = 6;
    private final int mDefaultBorderLineLength = 4;

    //add by hunt
    private String mHint = "";

    //    protected Paint mLaserPaint;
    protected Paint mFinderMaskPaint;
    protected Paint mBorderPaint;
    protected Paint mHintPaint;
    protected int mBorderLineLength;
//////////////////////////////////////////////////
    /**
     * 改变线条高度的动画
     */
    private ValueAnimator laserAnimation;

    //    private Rect laserRect = new Rect();
    private Paint linearGradientPaint = new Paint();
    private Paint linePaint = new Paint();
    /**
     * 底部线条颜色
     */
    private int effectStartColor = Color.parseColor("#b849b8FF");
    private int effectEndColor = Color.WHITE;
    private float lineWidth = 5;
    private int linePositionY = 0;
    private int animationDuration = 3000;

    //////////////////////////////////////////////////
    public ViewFinderView(Context context) {
        super(context);
        init();
    }

    public ViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        //finder mask paint
        mFinderMaskPaint = new Paint();
        mFinderMaskPaint.setColor(mDefaultMaskColor);

        //border paint
        mBorderPaint = new Paint();
        mBorderPaint.setColor(mDefaultBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mDefaultBorderStrokeWidth);


        mHintPaint = new Paint();
        mHintPaint.setAntiAlias(true);
        mHintPaint.setColor(mDefaultBorderColor);
        mHintPaint.setColor(Color.WHITE);
        mHintPaint.setTextSize(DensityUtils.dip2px(16));

        mBorderLineLength = mDefaultBorderLineLength;

        mTextRect = new Rect();

        linePaint.setColor(Color.parseColor("#ff49b8FF"));
        linePaint.setAntiAlias(true);
    }


    public void setMaskColor(int maskColor) {
        mFinderMaskPaint.setColor(maskColor);
    }

    public void setBorderColor(int borderColor) {
        mBorderPaint.setColor(borderColor);
    }

    public void setBorderStrokeWidth(int borderStrokeWidth) {
        mBorderPaint.setStrokeWidth(borderStrokeWidth);
    }

    public void setBorderLineLength(int borderLineLength) {
        mBorderLineLength = borderLineLength;
    }

    public void setupViewFinder() {
        updateFramingRect();
        invalidate();
    }

    public Rect getFramingRect() {
        return mFramingRect;
    }

    @Override
    public void setupHint(String content) {
        this.mHint = content;
        if (mHint == null || mHint.length() == 0)
            return;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mFramingRect == null) {
            return;
        }

        drawViewFinderMask(canvas);
        drawViewFinderBorder(canvas);
        drawHint(canvas);
        drawLaser(canvas);
    }

    private void drawLaser(Canvas canvas) {
//        if (linePositionY < mFramingRect.top || linePositionY > mFramingRect.bottom)
//            return;
        if (linePositionY == 0)
            return;
        int maxHeight = mFramingRect.height() / 2;
        int top = 0;
        if (linePositionY >= maxHeight) {
            top = linePositionY - maxHeight;
        }
        canvas.translate(mFramingRect.left, mFramingRect.top + top);
        canvas.drawRect(0,
                0,
                mFramingRect.width(),
                linePositionY - top,
                linearGradientPaint);
        canvas.drawRect(0,
                linePositionY - top,
                mFramingRect.width(),
                linePositionY + lineWidth - top, linePaint);
    }

    public void drawViewFinderMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        canvas.drawRect(0, 0, width, mFramingRect.top, mFinderMaskPaint);
        canvas.drawRect(0, mFramingRect.top, mFramingRect.left, mFramingRect.bottom + 1, mFinderMaskPaint);
        canvas.drawRect(mFramingRect.right + 1, mFramingRect.top, width, mFramingRect.bottom + 1, mFinderMaskPaint);
        canvas.drawRect(0, mFramingRect.bottom + 1, width, height, mFinderMaskPaint);
    }

    public void drawViewFinderBorder(Canvas canvas) {
        canvas.drawLine(mFramingRect.left - mDefaultBorderStrokeWidth / 2,
                mFramingRect.top - mDefaultBorderStrokeWidth / 2,
                mFramingRect.left + 5 * mDefaultBorderStrokeWidth,
                mFramingRect.top - mDefaultBorderStrokeWidth / 2,
                mBorderPaint);
        canvas.drawLine(mFramingRect.left - mDefaultBorderStrokeWidth / 2,
                mFramingRect.top - mDefaultBorderStrokeWidth,
                mFramingRect.left - mDefaultBorderStrokeWidth / 2,
                mFramingRect.top + 5 * mDefaultBorderStrokeWidth,
                mBorderPaint);

        canvas.drawLine(mFramingRect.left - mDefaultBorderStrokeWidth / 2,
                mFramingRect.bottom + mDefaultBorderStrokeWidth,
                mFramingRect.left - mDefaultBorderStrokeWidth / 2,
                mFramingRect.bottom - 5 * mDefaultBorderStrokeWidth,
                mBorderPaint);
        canvas.drawLine(mFramingRect.left - mDefaultBorderStrokeWidth / 2,
                mFramingRect.bottom + mDefaultBorderStrokeWidth / 2,
                mFramingRect.left + 5 * mDefaultBorderStrokeWidth,
                mFramingRect.bottom + mDefaultBorderStrokeWidth / 2, mBorderPaint);


        canvas.drawLine(mFramingRect.right + mDefaultBorderStrokeWidth / 2,
                mFramingRect.top - mDefaultBorderStrokeWidth / 2,
                mFramingRect.right - 5 * mDefaultBorderStrokeWidth,
                mFramingRect.top - mDefaultBorderStrokeWidth / 2, mBorderPaint);
        canvas.drawLine(mFramingRect.right + mDefaultBorderStrokeWidth / 2,
                mFramingRect.top - mDefaultBorderStrokeWidth,
                mFramingRect.right + mDefaultBorderStrokeWidth / 2,
                mFramingRect.top + 5 * mDefaultBorderStrokeWidth, mBorderPaint);

        canvas.drawLine(mFramingRect.right + mDefaultBorderStrokeWidth / 2,
                mFramingRect.bottom + mDefaultBorderStrokeWidth / 2,
                mFramingRect.right + mDefaultBorderStrokeWidth / 2,
                mFramingRect.bottom - 5 * mDefaultBorderStrokeWidth,
                mBorderPaint);
        canvas.drawLine(mFramingRect.right + mDefaultBorderStrokeWidth,
                mFramingRect.bottom + mDefaultBorderStrokeWidth / 2,
                mFramingRect.right - 5 * mDefaultBorderStrokeWidth,
                mFramingRect.bottom + mDefaultBorderStrokeWidth / 2, mBorderPaint);
    }

    public void drawHint(Canvas canvas) {
        mHintPaint.getTextBounds(mHint, 0, mHint.length(), mTextRect);
        final int l = getMeasuredWidth() / 2 - mTextRect.width() / 2;
        canvas.drawText(mHint, l, mFramingRect.bottom + 3 * mTextRect.height(), mHintPaint);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        updateFramingRect();
    }

    /**
     * 生成线性半透明
     */
    private void createShader() {
        Bitmap bitmap = Bitmap.createBitmap(mFramingRect.width(), mFramingRect.height() / 2,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        LinearGradient linearGradient = new LinearGradient(mFramingRect.width() / 2,
                mFramingRect.height() / 2,
                mFramingRect.width() / 2,
                0,
                effectStartColor,
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(linearGradient);
        canvas.drawRect(0, 0,
                mFramingRect.width(),
                mFramingRect.height(), paint);
        // use the bitmap to create the shader
        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        linearGradientPaint.setShader(bitmapShader);
    }

    public synchronized void updateFramingRect() {
        Point viewResolution = new Point(getWidth(), getHeight());
        int width;
        int height;
        int orientation = DisplayUtils.getScreenOrientation(getContext());

        if (orientation != Configuration.ORIENTATION_PORTRAIT) {
            width = findDesiredDimensionInRange(LANDSCAPE_WIDTH_RATIO, viewResolution.x, MIN_FRAME_WIDTH, LANDSCAPE_MAX_FRAME_WIDTH);
//            height = findDesiredDimensionInRange(LANDSCAPE_HEIGHT_RATIO, viewResolution.y, MIN_FRAME_HEIGHT, LANDSCAPE_MAX_FRAME_HEIGHT);
            height = width;
        } else {
            width = findDesiredDimensionInRange(PORTRAIT_WIDTH_RATIO, viewResolution.x, MIN_FRAME_WIDTH, PORTRAIT_MAX_FRAME_WIDTH);
//            height = findDesiredDimensionInRange(PORTRAIT_HEIGHT_RATIO, viewResolution.y, MIN_FRAME_HEIGHT, PORTRAIT_MAX_FRAME_HEIGHT);
            height = width;
        }

        int leftOffset = (viewResolution.x - width) / 2;
        int topOffset = (viewResolution.y - height) / 2 - 20;
        mFramingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        prepareLaserAnimation();
    }

    private void prepareLaserAnimation() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                createShader();
                startAnimation();
            }
        }, 1000);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    private static int findDesiredDimensionInRange(float ratio, int resolution, int hardMin, int hardMax) {
        int dim = (int) (ratio * resolution);
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    /**
     * 开始动画
     */
    public void startAnimation() {
        if (laserAnimation == null)
            laserAnimation = ValueAnimator.ofInt(0,
                    mFramingRect.height());
        if (laserAnimation.isRunning())
            return;
        laserAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (animation != null && (animation.getAnimatedValue() instanceof Integer)) {
                    linePositionY = (int) animation.getAnimatedValue();
                    invalidate();
                }
            }
        });
        laserAnimation.setDuration(animationDuration);
        laserAnimation.setInterpolator(new LinearInterpolator());
        laserAnimation.setRepeatCount(ValueAnimator.INFINITE);
        laserAnimation.start();
    }

    /**
     * 停止动画
     */
    public void stopAnimation() {
        if (laserAnimation != null && laserAnimation.isRunning())
            laserAnimation.cancel();
    }
}
