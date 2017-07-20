package com.cylan.jiafeigou.widget.wheel.ex;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-7-12.
 */
public class SuperWheelExt extends View {
    public static final int STATE_DRAGGING = 0;
    public static final int STATE_ADSORB = 1;//吸附
    public static final int STATE_FINISH = 2;
    public static boolean DEBUG = true;

    public static final String TAG = "SuperWheelExt";

    private ITouchHandler touchHandler;

    private Paint markerPaint = new Paint();

    private Paint naturalDateLinePaint = new Paint();
    private Paint naturalDateTextPaint = new Paint();

    private Paint dataMaskPaint = new Paint();

    private int totalCountInScreenSize = 0;

    private float minPosition = 0;
    /**
     * 线之间距离,10分钟一格.
     */
    private final float lineIntervalPx;
    /**
     * 线宽
     */
    private final int defaultLineWidth;

    /**
     * 一秒中多少像素
     */
    private float pixelsInSecond;

    private static final String DATE_IN_NORMAL = "12:30";
    private float dateTextSize = 10;
    private int dateTextWidth = 0;
    private Rect textRect;

    /**
     * 拖拽过程,一直回调返回时间
     */
    private boolean notifyAlways = true;

    private int LINE_HEIGHT_0 = 10;
    private int LINE_HEIGHT_1 = 25;

    private IData iDataProvider;

    public IData getDataProvider() {
        return iDataProvider;
    }

    public void setDataProvider(IData iDataProvider) {
        this.iDataProvider = iDataProvider;
        if (DEBUG)
            Log.d(TAG, "setDataProvider: " + iDataProvider.getDataCount());
        setScrollX(0);
        post(() -> {
            if (DEBUG)
                Log.d(TAG, "getScrollX: " + getScrollX());
            invalidate();
        });
    }


    public SuperWheelExt(Context context) {
        this(context, null);
    }

    public SuperWheelExt(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SuperWheelExt(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        defaultLineWidth = dp2px(2.5f);
        LINE_HEIGHT_0 = dp2px(LINE_HEIGHT_0);
        LINE_HEIGHT_1 = dp2px(LINE_HEIGHT_1);
        dateTextSize = dp2px(dateTextSize);
        //1s多少像素
        pixelsInSecond = 0.04f;
        lineIntervalPx = pixelsInSecond * 10 * 60;
        TypedArray at = context.obtainStyledAttributes(attrs, R.styleable.SWheelViewStyle);
        int markerColor = at.getColor(R.styleable.SWheelViewStyle_sw_markerColor, Color.BLUE);
        int maskColor = at.getColor(R.styleable.SWheelViewStyle_sw_maskColor, Color.BLUE);
        int lineColor = at.getColor(R.styleable.SWheelViewStyle_sw_lineColor, Color.BLUE);
        int textColor = at.getColor(R.styleable.SWheelViewStyle_sw_textColor, Color.BLUE);
        at.recycle();
        touchHandler = new ITouchHandler(this);
        markerPaint.setAntiAlias(true);
        markerPaint.setColor(markerColor);
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setStrokeWidth(defaultLineWidth);

        naturalDateLinePaint.setAntiAlias(true);
        naturalDateLinePaint.setColor(lineColor);
        naturalDateLinePaint.setStrokeWidth(defaultLineWidth);

        dataMaskPaint.setAntiAlias(true);
        dataMaskPaint.setColor(maskColor);

        naturalDateTextPaint.setAntiAlias(true);
        naturalDateTextPaint.setColor(textColor);
        naturalDateTextPaint.setTextSize(dateTextSize);
        textRect = new Rect();
        naturalDateTextPaint.getTextBounds(DATE_IN_NORMAL, 0, DATE_IN_NORMAL.length(), textRect);
        dateTextWidth = textRect.width();
    }

    private int dp2px(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return touchHandler.onTouchEvent(event);
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

    public boolean isBusy() {
        return touchHandler != null && touchHandler.isTouchDown() || !touchHandler.isFinished();
    }

    /**
     * 一次最多绘制N条
     *
     * @return
     */

    private int getFullScreenItemsCount() {
        if (totalCountInScreenSize > 0)
            return totalCountInScreenSize;
        final int doubleScreenSize = (getMeasuredWidth() * 3) >> 1;
        return totalCountInScreenSize = (int) (doubleScreenSize / lineIntervalPx);
    }

    public void setNotifyAlways(boolean notifyAlways) {
        this.notifyAlways = notifyAlways;
    }

    /**
     * 屏幕方向改变之后,需要更新
     */
    public void notifyOrientationChange() {
        totalCountInScreenSize = 0;
        minPosition = 0;
    }

    /**
     * long:时间戳
     *
     * @return
     */
    private long[] getRawTimeList() {
        //偏移N格,需要找出左节点,和右边节点.
        int offsetCount = (int) (-getScrollX() / lineIntervalPx);
        final int count = getFullScreenItemsCount();
        int totalCount = iDataProvider == null ? 0 : iDataProvider.getDataCount();
        int start = offsetCount >= count / 2 ? Math.abs(count / 2 - offsetCount) : 0;
        int end = totalCount - start > count ? count + start : totalCount;
        if (DEBUG)
            Log.d(TAG, String.format("offset:%s,count:%s,totalCount:%s,initSubscription:%s,end:%s",
                    offsetCount, count, totalCount, start, end));
        return iDataProvider == null ? new long[]{0, 0} : iDataProvider.getTimeArray(end, start);
    }

    /**
     * 保持marker在屏幕的位置不变。
     *
     * @return
     */
    private int getMarkerLeft() {
        return (getMeasuredWidth() >> 1) + getScrollX();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (iDataProvider == null || iDataProvider.getDataCount() == 0) {
            if (DEBUG)
                Log.d(TAG, "onDraw null");
            return;
        }
        drawDateSet(canvas);
        //画中间的marker
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
    private void drawDateSet(Canvas canvas) {
        long[] timeList = getRawTimeList();
        int size = timeList == null ? 0 : timeList.length;
        if (DEBUG)
            Log.d(TAG, "drawNaturalDateSet: " + size);
        int c = canvas.save();
        drawDataMask(canvas, timeList);
        canvas.restoreToCount(c);
        c = canvas.save();
        for (int i = 0; i < size; i++) {
            float pos = getPosition(timeList[i]);
//            if (DEBUG)
//                Log.d(TAG, "pos: " + pos);
            canvas.drawLine(pos,
                    0,
                    pos,
                    getLineBottomByType(timeList[i]),
                    naturalDateLinePaint);
            drawDateText(canvas, pos, timeList[i]);
        }
        canvas.restoreToCount(c);
    }

    private RectF rect = new RectF();

    private void drawDataMask(Canvas canvas, long[] timeList) {
        int count = timeList == null ? 0 : timeList.length;
        if (count > 0) {
            ArrayList<HistoryFile> list = iDataProvider.getMaskList(timeList[0], timeList[count - 1]);
            final int size = list == null ? 0 : list.size();
            for (int i = 0; i < size; i++) {
                HistoryFile v = list.get(i);
                float rectStart = getPosition(v.time * 1000L);
                float rectEnd = getPosition((v.time + v.duration) * 1000L);
                rect.left = rectStart;
                rect.right = rectEnd;
                rect.top = 0;
                rect.bottom = getHeight();
                canvas.drawRect(rect, dataMaskPaint);
                if (DEBUG)
                    Log.d("drawDataMask", "drawDataMask: " + rectStart + " " + rectEnd);
            }
        }
    }

    /**
     * 长度
     *
     * @param time
     * @return
     */
    private int getLineBottomByType(final long time) {
        return iDataProvider == null ? 0 : (iDataProvider.getBottomType(time) == 1 ? LINE_HEIGHT_1 : LINE_HEIGHT_0);
    }

    /**
     * 绘制 整点 时间
     *
     * @param canvas
     * @param time
     */
    private void drawDateText(Canvas canvas, final float pos, final long time) {
        if (time < 0 || iDataProvider == null || iDataProvider.getBottomType(time) == 0)
            return;
        canvas.drawText(iDataProvider.getDateInFormat(time),
                pos - (dateTextWidth >> 1),
                getHeight() + 2 * textRect.centerY(),
                naturalDateTextPaint);
    }

    /**
     * 通过时间获取pos
     *
     * @param time
     * @return
     */
    private float getPosition(long time) {
        long timeInterval = iDataProvider == null ? 0 : (iDataProvider.getFlattenMaxTime() - time);
        return -timeInterval * pixelsInSecond / 1000.0f + getMeasuredWidth() / 2;//中心点为0,像左边降序.
    }

    public int getMaxScrollX() {
        if (iDataProvider == null) return 0;
        return (int) ((getMeasuredWidth() >> 1) - getMinPos(iDataProvider.getFlattenMinTime()));
    }

    public int getMinScrollX() {
        return getMeasuredWidth() / 2;
    }

    /**
     * 计算当前的时间.
     *
     * @return
     */
    public long getCurrentFocusTime() {
        int scrollX = getScrollX();
        long timeDelta = (int) (scrollX / pixelsInSecond) * 1000L;
        return iDataProvider == null ? 0 : iDataProvider.getFlattenMaxTime() + timeDelta;
    }

    private long tmpCurrentTime = 0;

    /**
     * fling,松手后调用
     *
     * @param newState
     * @param moveDirection
     */
    public void autoSettle(int newState, @ITouchHandler.MoveDirection int moveDirection) {
        if (iDataProvider != null) {
            //通过
            boolean idle = newState == ITouchHandler.SCROLL_STATE_IDLE;//判断当前的位置是否是热区,即:mask区域.
            long timeCurrent = getCurrentFocusTime();
            if (tmpCurrentTime == timeCurrent) {
                if (idle && !iDataProvider.isHotRect(timeCurrent))//dragging finish,空白区域
                {
                    long timeTarget = iDataProvider.getNextFocusTime(timeCurrent, moveDirection);
                    setPositionByTime(timeTarget);
                    Log.d("tmpCurrentTime", "tmpCurrentTime==");
                }
                return;
            }
            tmpCurrentTime = timeCurrent;
            long timeTarget = iDataProvider.getNextFocusTime(timeCurrent, moveDirection);
            Log.d("timeCurrent", "timeCurrent: " + timeCurrent);
            Log.d("timeCurrent", "timeTarget: " + timeTarget);
            if (moveDirection != ITouchHandler.MoveDirection.NONE && idle) {
                //开始吸附过程
                if (wheelRollListener != null)
                    wheelRollListener.onWheelTimeUpdate(timeCurrent, STATE_ADSORB);
                setPositionByTime(timeTarget);
                if (wheelRollListener != null)
                    wheelRollListener.onWheelTimeUpdate(timeCurrent, STATE_FINISH);//回调的应该是 target 的
            } else {
                if (notifyAlways && !idle && (wheelRollListener != null)) {
                    wheelRollListener.onWheelTimeUpdate(timeCurrent, STATE_DRAGGING);
                } else {
                    if (!idle)
                        return;
                    if (iDataProvider.isHotRect(timeCurrent)) {
                        //拖拽停止.
                        if (wheelRollListener != null)
                            wheelRollListener.onWheelTimeUpdate(timeCurrent, STATE_FINISH);
                        if (DEBUG)
                            Log.d(TAG, "hit");
                    } else {
                        //可能需要恢复到起点或者最后的点,因为这个两侧的区域,超出了数据的范围.
                        if (timeCurrent > iDataProvider.getFlattenMaxTime() || timeCurrent < iDataProvider.getFlattenMinTime()) {
                            float deltaDx = (timeTarget - timeCurrent) / 1000L * pixelsInSecond;
                            touchHandler.startSmoothScroll(getScrollX(), (int) deltaDx);
                            return;
                        }
                    }
                }
            }
            if (DEBUG)
                Log.d(TAG, String.format("idle:%s,direction:%s", idle, moveDirection));
        }
    }

    /**
     * drag拖动过程
     *
     * @param newState
     */
    public void autoSettle(int newState) {
        autoSettle(newState, ITouchHandler.MoveDirection.NONE);
    }

    /**
     * 通过时间来定位
     *
     * @param timeTarget
     */
    public void setPositionByTime(long timeTarget) {
//        post(() -> {
        long timeCurrent = getCurrentFocusTime();
        float deltaDx = (timeTarget - timeCurrent) / 1000L * pixelsInSecond;
        touchHandler.startSmoothScroll(getScrollX(), (int) deltaDx);
//        });
    }

    /**
     * 通过时间计算位置
     *
     * @param time
     * @return
     */
    private float getMinPos(long time) {
        if (minPosition > 0) return minPosition;
        return minPosition = getPosition(time);
    }

    public void setWheelRollListener(WheelRollListener wheelRollListener) {
        this.wheelRollListener = wheelRollListener;
    }

    private WheelRollListener wheelRollListener;

    public interface WheelRollListener {
        /**
         * @param time: 移动后，
         */
        void onWheelTimeUpdate(long time, int state);
    }
}

