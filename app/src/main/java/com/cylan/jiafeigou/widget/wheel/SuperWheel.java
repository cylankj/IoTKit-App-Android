package com.cylan.jiafeigou.widget.wheel;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 16-7-12.
 */
public class SuperWheel extends View {
    private boolean DEBUG = false;

    /**
     * Indicates that the pager is in an idle, settled state. The current page
     * is fully in view and no animation is in progress.
     */
    public static final int SCROLL_STATE_IDLE = 0;

    /**
     * Indicates that the pager is currently being dragged by the user.
     */
    public static final int SCROLL_STATE_DRAGGING = 1;

    /**
     * Indicates that the pager is in the process of settling to activity_cloud_live_mesg_video_talk_item final position.
     */
    public static final int SCROLL_STATE_SETTLING = 2;

    public static final String TAG = "SuperWheel";
    private static final String[] sample = {};

    private STouchHandler touchHandler;

    private Paint markerPaint = new Paint();

    private Paint naturalDateLinePaint = new Paint();
    private Paint naturalDateTextPaint = new Paint();

    private Paint dataMaskPaint = new Paint();

    private final int lineIntervalPx;
    private final int defaultLineWidth;

    private SDataStack dataStack;

    private int dataCount;

    private static final String DATE_IN_NORMAL = "12:30";
    private float dateTextSize = 10;
    private int dateTextWidth = 0;
    private Rect textRect;

    private int LINE_HEIGHT_0 = 10;
    private int LINE_HEIGHT_1 = 25;

    /**
     * 一秒多少像素
     */
    private float secondPx;

    public SuperWheel(Context context) {
        this(context, null);
    }

    public SuperWheel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SuperWheel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray at = context.obtainStyledAttributes(attrs, R.styleable.SWheelViewStyle);
        float shortLineHeight = at.getDimension(R.styleable.SWheelViewStyle_sw_shortLineHeight, 10.0f);
        float longLineHeight = at.getDimension(R.styleable.SWheelViewStyle_sw_longLineHeight, 25);
        float lineInterval = at.getDimension(R.styleable.SWheelViewStyle_sw_lineIntervalWidth, 10);
        float lineWidth = at.getDimension(R.styleable.SWheelViewStyle_sw_lineWidth, 2.5f);
        int textSize = at.getDimensionPixelSize(R.styleable.SWheelViewStyle_sw_dateTextSize, 11);
        int markerColor = at.getColor(R.styleable.SWheelViewStyle_sw_markerColor, Color.BLUE);
        at.recycle();
        lineIntervalPx = dp2px(lineInterval);
        defaultLineWidth = dp2px(lineWidth);
        LINE_HEIGHT_0 = dp2px(shortLineHeight);
        LINE_HEIGHT_1 = dp2px(longLineHeight);
        dateTextSize = dp2px(textSize);
        touchHandler = new STouchHandler(this);

        markerPaint.setAntiAlias(true);
        markerPaint.setColor(markerColor);
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setStrokeWidth(defaultLineWidth);


        naturalDateLinePaint.setAntiAlias(true);
        naturalDateLinePaint.setColor(0xFFDEDEDE);
        naturalDateLinePaint.setStrokeWidth(defaultLineWidth);

        dataMaskPaint.setAntiAlias(true);
        dataMaskPaint.setColor(0xCCE1F0FF);
        initDateText();
    }

    private void initDateText() {
        naturalDateTextPaint.setAntiAlias(true);
        naturalDateTextPaint.setColor(0xFFAAAAAA);
        naturalDateTextPaint.setTextSize(dateTextSize);
        textRect = new Rect();
        naturalDateTextPaint.getTextBounds(DATE_IN_NORMAL, 0, DATE_IN_NORMAL.length(), textRect);
        dateTextWidth = textRect.width();
    }


    public void setDataStack(SDataStack dataStack) {
        if (dataStack == null
                || dataStack.naturalDateSet == null
                || dataStack.naturalDateSet.size() == 0)
            return;
        this.dataStack = dataStack;
        dataCount = dataStack.naturalDateSet.size();
        assembleNaturalDatePosition(dataStack);
        calculateSecondPx();
        assembleRecordPosition();
        invalidate();
    }

    private void calculateSecondPx() {
        //计算每一个像素的
        secondPx = ((dataStack.naturalDateSet.get(dataCount - 1) - dataStack.naturalDateSet.get(0)) / 1000)
                / (dataStack.naturalDateSetPosition[dataCount - 1] - dataStack.naturalDateSetPosition[0]);
        if (DEBUG)
            Log.d(TAG, "pxSecond: " + secondPx);
        //20s  px
    }

    /**
     * 计算 历史记录的位置
     */
    private void assembleRecordPosition() {
        final int count = dataStack.recordTimeSet.length;
        dataStack.recordTimePositionSet = new float[count];
        final int startX = getMeasuredWidth() >> 1;
        for (int i = count - 1; i >= 0; i--) {
            dataStack.recordTimePositionSet[i] = (int) (startX -
                    (dataStack.naturalDateSet.get(dataCount - 1) -
                            dataStack.recordTimeSet[i]) / 1000.0f / secondPx);
        }
//        dataStack.recordTimePositionSet[0] = dataStack.naturalDateSetPosition[0];
    }

    /**
     * 计算 自然日期的位置
     *
     * @param dataStack
     */
    public void assembleNaturalDatePosition(SDataStack dataStack) {
        final int dataCount = dataStack.naturalDateSet.size();
        dataStack.naturalDateSetPosition = new float[dataCount];
        for (int i = dataCount - 1; i >= 0; i--) {
            dataStack.naturalDateSetPosition[i] = (getMeasuredWidth() >> 1)
                    - (lineIntervalPx) * (dataCount - i - 1);
        }
    }

    private int dp2px(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return checkData() && touchHandler.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        touchHandler.computeScroll();
        super.computeScroll();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        getFullScreenItemsCount();
    }


    /**
     * 一次最多绘制N条
     *
     * @return
     */

    private int getFullScreenItemsCount() {
        final int maxWidthSize = (getMeasuredWidth() * 3) >> 1;
        return maxWidthSize / lineIntervalPx;
    }

    /**
     * 找出左边第一条需要绘制的index;大量数据 只需要画出在屏幕范围之内的数据。
     *
     * @return
     */
    private int[] findNeedIndex() {
        final long time = System.currentTimeMillis();
        //find left side
        int leftIndex = (int) (1 - (getMeasuredWidth() / 4 - getScrollX() + dataStack.naturalDateSetPosition[0]) / (lineIntervalPx));
        if (DEBUG)
            Log.d(TAG, "performance: " + (System.currentTimeMillis() - time));
        int array[] = new int[2];
        array[0] = leftIndex < 0 ? 0 : leftIndex;
        final int count = getFullScreenItemsCount();
        array[1] = leftIndex + count > dataCount - 1 ? dataCount : leftIndex + count;
        return array;
    }

    /**
     * 保持marker在屏幕的位置不变。
     *
     * @return
     */
    private int getMarkerLeft() {
        return (getMeasuredWidth() >> 1) + getScrollX();
    }

    private boolean checkData() {
        return dataStack != null
                && dataStack.naturalDateSetPosition != null
                && dataStack.naturalDateSetPosition.length > 0
                && dataStack.naturalDateSet != null
                && dataStack.naturalDateSet.size() > 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (checkData()) {
            drawRecordMask(canvas);
            drawNaturalDateSet(canvas);
        }
        canvas.drawLine(getMarkerLeft(),
                0,
                getMarkerLeft(),
                getBottom(),
                markerPaint);
    }

    /**
     * 绘制背景时间戳
     *
     * @param canvas
     */
    private void drawNaturalDateSet(Canvas canvas) {
        final int index[] = findNeedIndex();
        for (int i = index[0]; i < index[1]; i++) {
            canvas.drawLine(dataStack.naturalDateSetPosition[i],
                    0,
                    dataStack.naturalDateSetPosition[i],
                    getLineBottomByType(i),
                    naturalDateLinePaint);
            drawDateText(canvas, i);
        }
    }

    /**
     * 长度
     *
     * @param index
     * @return
     */
    private int getLineBottomByType(final int index) {
        return dataStack.naturalDateType[index] == 0 ? LINE_HEIGHT_1 : LINE_HEIGHT_0;
    }

    /**
     * 绘制 整点 时间
     *
     * @param canvas
     * @param index
     */
    private void drawDateText(Canvas canvas, final int index) {
        if (index < 0 || index >= dataCount || dataStack.naturalDateType[index] == 1)
            return;
        canvas.drawText(dataStack.dateStringMap.get("" + index),
                dataStack.naturalDateSetPosition[index] - (dateTextWidth >> 1),
                getHeight() + 2 * textRect.centerY(),
                naturalDateTextPaint);
    }

    /**
     * 绘制 有数据的区域
     *
     * @param canvas
     */
    private void drawRecordMask(Canvas canvas) {
        final int recordMaskCount = dataStack.recordTimePositionSet.length;
        for (int i = 0; i < recordMaskCount - 1; ) {
            canvas.drawRect(dataStack.recordTimePositionSet[i],
                    0, dataStack.recordTimePositionSet[i + 1],
                    getHeight(), dataMaskPaint);
            i += 2;
        }
    }


    public int getMaxScrollX() {
        return (int) ((getMeasuredWidth() >> 1) - dataStack.naturalDateSetPosition[0]);
    }

    public void autoSettle(String scrollStateIdle, int moveDirection) {
        if (DEBUG)
            Log.d(TAG, "should auto settle ");
        if (wheelRollListener != null && checkData()) {
            final long preFocusTime = dataStack.naturalDateSet.get(dataCount - 1)
                    + (long) (getScrollX() * secondPx * 1000L);
            wheelRollListener.onSettleFinish(preFocusTime);
        }
    }


    /**
     * 外部传入的时间
     *
     * @param time
     */
    public void updateScrollX(final long time) {
        if (time > dataStack.naturalDateSet.get(dataCount - 1)
                || time < dataStack.naturalDateSet.get(0))
            return;
        float distanceFromStart = -(dataStack.naturalDateSet.get(dataCount - 1) - time) / 1000 / secondPx;
        final float needOffsetX = distanceFromStart - getScrollX();
        scrollBy((int) needOffsetX, 0);
    }

    public void updateScrollX() {
        final long preFocusTime = dataStack.naturalDateSet.get(dataCount - 1)
                + (long) (getScrollX() * secondPx * 1000L);
        if (wheelRollListener != null)
            wheelRollListener.onTimeUpdate(preFocusTime);
    }

    public void setWheelRollListener(WheelRollListener wheelRollListener) {
        this.wheelRollListener = wheelRollListener;
    }

    private WheelRollListener wheelRollListener;

    public interface WheelRollListener {
        /**
         * @param time: 移动后，
         */
        void onTimeUpdate(long time);

        void onSettleFinish(long time);
    }
}

