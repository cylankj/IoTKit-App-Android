package com.cylan.jiafeigou.widget.wheel;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NavigableSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanzhendong on 2017/12/22.
 */

public class HistoryWheelView extends View implements GestureDetector.OnGestureListener {
    private static final String TAG = HistoryWheelView.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private OverScroller mScroller;
    private GestureDetectorCompat mDetector;

    private Paint markerPaint = new Paint();
    private Paint naturalDateLinePaint = new Paint();
    private Paint naturalDateTextPaint = new Paint();
    private Paint dataMaskPaint = new Paint();
    private int mTouchSlop;
    private volatile boolean mLocked = false;
    private volatile boolean mHasPendingUpdateAction = false;
    private volatile boolean mHasPendingSnapAction = false;
    private int markerColor;
    private int maskColor;
    private int lineColor;
    private int textColor;
    private int lineInterval;
    private int lineWidth;
    private int textSize;
    private int shortLineHeight;
    private int longLineHeight;
    /**
     * 在内部更新或外部带锁定操作更新后,锁定历史时间轴一段时间,在这段时间里
     * 外部更新将会被忽略,内部更新任何时候是不受影响的
     */
    private int scrollerLockTime;
    /***
     *滑动停止后延迟一定的时间再通知更新,这样做的原因是用户可能在连续滑动
     *如果滑动一停止立即通知更新,可能不是所希望的结果,延迟一段时间以确保
     *用户没有在继续操作了
     */
    private int updateDelay;
    /***
     *标定刻度是否居于屏幕正中间,如果为 true 标定刻度将居于屏幕正中,否则居于控件正中
     */
    private boolean markerCenterInScreen;
    private int mCenterPosition;
    private int textTopMargin;
    private int textBottomMargin;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private double fontHeight;
    private EdgeEffect mEdgeEffectLeft;
    private EdgeEffect mEdgeEffectRight;
    private boolean mOverScrollerMode = true;

    @IntDef({SnapDirection.MOVE_DIRECTION, SnapDirection.LEFT, SnapDirection.RIGHT, SnapDirection.AUTO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SnapDirection {
        int MOVE_DIRECTION = -1;
        int LEFT = 0;
        int RIGHT = 1;
        int AUTO = 2;
    }

    private int mSnapDirection = SnapDirection.RIGHT;
    /**
     * 用来标记控件内部是否可以动态改变 mSnapDirection,在 onScroll 或者 onFling 方法里会
     * 读取这个字段,如果SnapDirection 没有被锁住,onScroll 或者 onFling 会根据手指滑动的
     * 方向动态的改变 SnapDirection,否则将按照外部设定的 SnapDirection 进行吸附
     */
    private boolean mSnapDirectionLocked = false;
    private Calendar mCalendar = Calendar.getInstance();
    private long mZeroTime;
    private Comparator<JFGVideo> mHistoryComparator = new Comparator<JFGVideo>() {
        @Override
        public int compare(JFGVideo o1, JFGVideo o2) {
            return (int) (o1.beginTime - o2.beginTime);
        }
    };
    private TreeSet<JFGVideo> mHistoryFiles = new TreeSet<>(mHistoryComparator);
    private Runnable mUnlockRunnable = new Runnable() {
        @Override
        public void run() {
            mLocked = false;
        }
    };


    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            if (mHistoryFiles.size() == 0) {
                return;
            }
            if (mHistoryListener != null) {
                long currentTime = getCurrentTime();
                long unitTime = getUnitTime();
                if (DEBUG) {
                    Log.d(TAG, "notify time is" + new Date(currentTime).toLocaleString());
                }
                mHistoryListener.onHistoryTimeChanged(currentTime / unitTime * unitTime);
            }
        }
    };

    private Runnable mMarkerPositionRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mScroller.isFinished()) {
                removeCallbacks(mMarkerPositionRunnable);
                post(mMarkerPositionRunnable);
                return;
            }
            long currentTime = getCurrentTime();
            if (markerCenterInScreen) {
                int[] location = new int[2];
                getLocationOnScreen(location);
                mCenterPosition = getResources().getDisplayMetrics().widthPixels / 2 - location[0];
            } else {
                mCenterPosition = getMeasuredWidth() / 2;
            }
            scrollToPositionInternal(currentTime);
        }
    };

    private HistoryListener mHistoryListener;


    public HistoryWheelView(Context context) {
        this(context, null);
    }

    public HistoryWheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mScroller = new OverScroller(getContext());
        mDetector = new GestureDetectorCompat(getContext(), this);
        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mZeroTime = mCalendar.getTimeInMillis();

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.HistoryWheelView);
        markerColor = attributes.getColor(R.styleable.HistoryWheelView_hw_marker_color, Color.parseColor("#36BDFF"));
        markerCenterInScreen = attributes.getBoolean(R.styleable.HistoryWheelView_hw_marker_on_screen_center, true);
        maskColor = attributes.getColor(R.styleable.HistoryWheelView_hw_mask_color, Color.parseColor("#4036BDFF"));
        textColor = attributes.getColor(R.styleable.HistoryWheelView_hw_text_color, Color.parseColor("#FFAAAAAA"));
        textSize = attributes.getDimensionPixelSize(R.styleable.HistoryWheelView_hw_text_size, dp2px(10));
        lineColor = attributes.getColor(R.styleable.HistoryWheelView_hw_line_color, Color.parseColor("#FFDEDEDE"));
        lineInterval = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_line_interval, (int) (0.04 * 10 * 60));
        lineWidth = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_line_width, dp2px(2.5f));
        shortLineHeight = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_short_line_height, dp2px(10));
        longLineHeight = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_long_line_height, dp2px(25));
        scrollerLockTime = attributes.getInteger(R.styleable.HistoryWheelView_hw_scroller_lock_time, 5_000);//锁的时间够久以确保历史录像有足够的时间切换
        updateDelay = attributes.getInteger(R.styleable.HistoryWheelView_hw_history_update_delay, 700);
        textTopMargin = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_history_text_top_margin, dp2px(5));
        textBottomMargin = attributes.getDimensionPixelSize(R.styleable.HistoryWheelView_hw_history_text_bottom_margin, dp2px(5));
        markerPaint.setAntiAlias(true);
        markerPaint.setColor(markerColor);
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setStrokeWidth(lineWidth);

        naturalDateLinePaint.setAntiAlias(true);
        naturalDateLinePaint.setColor(lineColor);
        naturalDateLinePaint.setStrokeWidth(lineWidth);

        dataMaskPaint.setAntiAlias(true);
        dataMaskPaint.setColor(maskColor);

        naturalDateTextPaint.setAntiAlias(true);
        naturalDateTextPaint.setColor(textColor);
        naturalDateTextPaint.setTextSize(textSize);
        naturalDateTextPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = naturalDateTextPaint.getFontMetrics();
        fontHeight = Math.ceil(fm.descent - fm.ascent);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mEdgeEffectLeft = new EdgeEffect(getContext());
        mEdgeEffectRight = new EdgeEffect(getContext());
        attributes.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /*
         *之所以用 post 是因为在这里直接获取的话获取不到控件在屏幕上的位置
         */
        removeCallbacks(mMarkerPositionRunnable);
        post(mMarkerPositionRunnable);
    }

    private int dp2px(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    private volatile float mDistanceX = 0;

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mDistanceX += distanceX;
        /*
         *当 distanceX 的距离小于 touchSlop 时,滑动没有效果,所有这里要积攒到最小到 touchSlop 才开始滑动
         */
        if (Math.abs(mDistanceX) < mTouchSlop) {
            return true;
        }

        disableExternalScrollAction();
        mHasPendingUpdateAction = true;
        if (mSnapDirection == SnapDirection.MOVE_DIRECTION || !mSnapDirectionLocked) {
            mHasPendingSnapAction = true;
        }
        distanceX = mDistanceX;
        if (!mSnapDirectionLocked) {
            mSnapDirection = distanceX >= 0 ? SnapDirection.RIGHT : SnapDirection.LEFT;
        }
        mDistanceX = 0;

        if (mOverScrollerMode) {
            float exceptX = mScroller.getCurrX() + distanceX;
            float finalX = distanceX > 0 ? Math.min(getMaxScrollX(), exceptX) : Math.max(getMinScrollX(), exceptX);
            distanceX = finalX - mScroller.getCurrX();
        }

        if (distanceX != 0) {
            mScroller.startScroll(mScroller.getCurrX(), 0, (int) distanceX, 0);
            invalidate();
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        disableExternalScrollAction();
        mHasPendingUpdateAction = true;
        if (mSnapDirection == SnapDirection.MOVE_DIRECTION || !mSnapDirectionLocked) {
            mHasPendingSnapAction = true;
        }
        if (!mSnapDirectionLocked) {
            mSnapDirection = velocityX <= 0 ? SnapDirection.RIGHT : SnapDirection.LEFT;
        }
        int minX = Integer.MIN_VALUE;
        int maxX = Integer.MAX_VALUE;
        if (mOverScrollerMode) {
            minX = getMinScrollX();
            maxX = getMaxScrollX();
        }
        mScroller.fling(mScroller.getCurrX(), 0, (int) -velocityX, 0, minX, maxX, 0, 0, 100, 0);
        invalidate();
        return true;
    }

    private int getMinScrollX() {
        long minTime = mZeroTime;
        if (mHistoryFiles.size() > 0) {
            JFGVideo first = mHistoryFiles.first();
            minTime = first.beginTime * getUnitTime();
        }
        return (int) (getDistanceByTime(minTime, mZeroTime) - mCenterPosition);
    }

    private int getMaxScrollX() {
        long emptyMaxTime = mZeroTime + getPixelTime() * getMeasuredWidth();
        long maxTime = emptyMaxTime;
        if (mHistoryFiles.size() > 0) {
            JFGVideo last = mHistoryFiles.last();
            maxTime = (last.beginTime + last.duration) * getUnitTime();
        }
        return (int) (getDistanceByTime(maxTime, emptyMaxTime) + (getMeasuredWidth() - mCenterPosition));
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mHistoryListener != null && mHasPendingUpdateAction && !mScroller.isOverScrolled()) {
                //非常没必要,但测试说要加
                mHistoryListener.onScrolling(getCurrentTime());
            }
            scrollTo(mScroller.getCurrX(), 0);
            invalidate();
        } else {
            notifyScrollCompleted();
        }
    }

    private void disableExternalScrollAction() {
        this.mLocked = true;
        removeCallbacks(mUnlockRunnable);
    }

    private void enableExternalScrollAction() {
        removeCallbacks(mUnlockRunnable);
        postDelayed(mUnlockRunnable, scrollerLockTime);
    }

    public long getCurrentTime() {
        return getPixelTime() * (mScroller.getCurrX() + mCenterPosition) + mZeroTime;
    }

    public void scrollToPosition(long time, boolean locked) {
        scrollToPosition(time, locked, false);
    }

    public void scrollToPosition(long time, boolean locked, boolean focus) {
        if (!mLocked || focus) {
            if (locked) {
                disableExternalScrollAction();
            }
            scrollToPositionInternal(time);
        }
    }

    public void snapScrollToPosition(long time, boolean locked) {
        long currentTime = getCurrentTime();
        time += getSnapDistanceX(time, time >= currentTime ? SnapDirection.RIGHT : SnapDirection.LEFT);
        scrollToPosition(time, locked, false);
    }

    private void scrollToPositionInternal(long time) {
        if (time == 0) {
            time = mZeroTime;
        }
        long currentTime = getCurrentTime();
        long distance = getDistanceByTime(time, currentTime);
        if (DEBUG) {
            Log.d(TAG, "scroller time is:" + new Date(time).toLocaleString() +
                    ",current time is:" + new Date(currentTime).toLocaleString() +
                    ",scroller distance is:" + distance +
                    ",currentX:" + mScroller.getCurrX() +
                    ",target time is:" + new Date(currentTime + distance * getPixelTime()).toLocaleString());
        }

        if (distance != 0) {
            removeCallbacks(mNotifyRunnable);
            mScroller.startScroll(mScroller.getCurrX(), 0, (int) distance, 0);
            invalidate();
        } else {
            notifyUpdate();
        }
    }

    private JFGVideo mSnapHistoryBlock = new JFGVideo("", 0, 0);

    private long getSnapDistanceX(long start, int snapDirection) {
        if (mHistoryFiles.size() == 0) {
            return 0;
        }
        long unitTime = getUnitTime();
        mSnapHistoryBlock.beginTime = start / unitTime;
        JFGVideo floor = mHistoryFiles.floor(mSnapHistoryBlock);
        JFGVideo ceiling = mHistoryFiles.ceiling(mSnapHistoryBlock);
        if (floor == null) {
            floor = mHistoryFiles.first();
        }
        if (ceiling == null) {
            ceiling = mHistoryFiles.last();
        }

        //判断当前是否需要做吸附操作,如果当前在数据区,则不需要做吸附操作了
        boolean inFloor = mSnapHistoryBlock.beginTime >= floor.beginTime && mSnapHistoryBlock.beginTime <= floor.beginTime + floor.duration;
        boolean inCeiling = mSnapHistoryBlock.beginTime >= ceiling.beginTime && mSnapHistoryBlock.beginTime <= ceiling.beginTime + ceiling.duration;
        if (inFloor || inCeiling) {
            return 0;
        }
        long distance = start;

        //在边界处是没有吸附规则的自动吸附到第一个或最后一个的开始处
        if (mSnapHistoryBlock.beginTime < floor.beginTime) {//处理左边界的情况
            distance = (mSnapHistoryBlock.beginTime - floor.beginTime) * unitTime;
        } else if (mSnapHistoryBlock.beginTime > ceiling.beginTime * ceiling.duration) {//处理右边界的情况
            distance = (mSnapHistoryBlock.beginTime - ceiling.beginTime) * unitTime;
        } else if (snapDirection == SnapDirection.LEFT) {//处理左吸附的情况
            distance = (mSnapHistoryBlock.beginTime - (floor.beginTime + floor.duration)) * unitTime;
        } else if (snapDirection == SnapDirection.RIGHT) {//处理右吸附的情况
            distance = (mSnapHistoryBlock.beginTime - ceiling.beginTime) * unitTime;
        } else if (snapDirection == SnapDirection.AUTO) {//处理最短吸附的情况
            long distanceF = mSnapHistoryBlock.beginTime - (floor.beginTime + floor.duration);
            long distanceC = mSnapHistoryBlock.beginTime - ceiling.beginTime;
            distance = start - (Math.abs(distanceC) < Math.abs(distanceF) ? distanceC : distanceF) * unitTime;
        }
        return distance;
    }

    private void notifySnapAction() {
        mHasPendingSnapAction = false;
        long currentTime = getCurrentTime();
        long distanceX = getSnapDistanceX(currentTime, mSnapDirection);
        if (distanceX == 0) {
            notifyUpdate();
        } else {
            scrollToPositionInternal(currentTime - distanceX);
        }
    }

    private void notifyUpdate() {
        enableExternalScrollAction();
        if (mHasPendingUpdateAction) {
            mHasPendingUpdateAction = false;
            removeCallbacks(mNotifyRunnable);
            postDelayed(mNotifyRunnable, updateDelay);
        }
    }

    private void notifyScrollCompleted() {
        if (mHasPendingSnapAction) {
            notifySnapAction();
        } else {
            notifyUpdate();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawHistoryBlock(canvas);
        drawBackground(canvas);
        drawTimeText(canvas);
        drawDivider(canvas);
        if (mOverScrollerMode) {
            drawEdgeEffect(canvas);
        }
    }


    private JFGVideo mStartHistoryBlock = new JFGVideo("", 0, 0);
    private JFGVideo mStopHistoryBlock = new JFGVideo("", 0, 0);

    private long getPixelTime() {
        return (long) (10 * 60 * 1000F / lineInterval);
    }


    private void drawHistoryBlock(Canvas canvas) {
        int startX = mScroller.getCurrX() - getMeasuredWidth() / 2;
        int stopX = startX + getMeasuredWidth() * 2;
        long unitTime = getUnitTime();
        mStartHistoryBlock.beginTime = (startX * getPixelTime() + mZeroTime) / unitTime;
        mStopHistoryBlock.beginTime = (stopX * getPixelTime() + mZeroTime) / unitTime;
        JFGVideo start = mHistoryFiles.floor(mStartHistoryBlock);
        if (start == null && mHistoryFiles.size() > 0) {
            start = mHistoryFiles.first();
        }
        JFGVideo stop = mHistoryFiles.ceiling(mStopHistoryBlock);
        if (stop == null && mHistoryFiles.size() > 0) {
            stop = mHistoryFiles.last();
        }
        try {
            if (mHistoryFiles.size() > 0) {
                NavigableSet<JFGVideo> historyFiles = mHistoryFiles.subSet(start, true, stop, true);
                for (JFGVideo file : historyFiles) {
                    long distanceX1 = getDistanceByTime(file.beginTime * unitTime, mZeroTime);
                    long distanceX2 = getDistanceByTime((file.beginTime + file.duration) * unitTime, mZeroTime);
                    canvas.drawRect(distanceX1, 0, Math.max(distanceX1 + 1, distanceX2)/*最少要画一个像素,否则还以为没有数据*/, getMeasuredHeight(), dataMaskPaint);
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.d(TAG, "迭代过程中数据发生了变化,需要重新绘制");
            }
            postInvalidate();
        }
    }

    private long getDistanceByTime(long start, long end) {
        return (start - end) / getPixelTime();
    }

    private void drawDivider(Canvas canvas) {
        int divider = mScroller.getCurrX() + mCenterPosition;
        canvas.drawLine(divider, 0, divider, getMeasuredHeight(), markerPaint);

    }

    private void drawBackground(Canvas canvas) {
        int startX = (mScroller.getCurrX() - getMeasuredWidth() / 2) / lineInterval * lineInterval;
        int stopX = startX + getMeasuredWidth() * 2;
        while (startX <= stopX) {
            canvas.drawLine(startX, 0, startX, startX % (lineInterval * 6) == 0 ? longLineHeight : shortLineHeight, naturalDateLinePaint);
            startX += lineInterval;
        }
    }

    private void drawEdgeEffect(Canvas canvas) {
        EdgeEffect edgeEffect = new EdgeEffect(getContext());
        edgeEffect.draw(canvas);
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private long getUnitTime() {
        long unitTime = 1;
        if (timeUnit == TimeUnit.SECONDS) {
            unitTime = 1000;
        }
        return unitTime;
    }

    private void drawTimeText(Canvas canvas) {
        int startX = (mScroller.getCurrX() - getMeasuredWidth() / 2) / lineInterval * lineInterval;
        int stopX = startX + getMeasuredWidth() * 2;
        while (startX <= stopX) {
            if (startX % (lineInterval * 6) == 0) {
                long distanceTime = startX * getPixelTime();
                int exceptY = longLineHeight + textTopMargin;
                int maxY = (int) (getMeasuredHeight() - textBottomMargin - fontHeight);
                canvas.drawText(dateFormat.format(mZeroTime + distanceTime), startX, Math.max(exceptY, maxY), naturalDateTextPaint);
            }
            startX += lineInterval;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return mDetector.onTouchEvent(event);
    }

    public void setTimeZone(TimeZone timeZone) {
        dateFormat.setTimeZone(timeZone);
        postInvalidate();
    }

    public void setSnapDirection(@SnapDirection int snapDirection) {
        this.mSnapDirection = snapDirection;
        this.mSnapDirectionLocked = snapDirection != SnapDirection.MOVE_DIRECTION;
    }


    public void setHistoryFiles(Collection<JFGVideo> historyFiles) {
        modifyHistoryInternal(historyFiles, true);
    }

    private void modifyHistoryInternal(Collection<JFGVideo> historyFiles, boolean clean) {
        HandlerThreadUtils.post(new Runnable() {
            @Override
            public void run() {
                if (clean) {
                    mHistoryFiles.clear();
                }
                mHistoryFiles.addAll(historyFiles);
                postInvalidate();
            }
        });
    }

    public void addHistoryFiles(List<JFGVideo> historyFiles) {
        modifyHistoryInternal(historyFiles, false);
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getHistoryCount() {
        return mHistoryFiles.size();
    }

    public boolean isLocked() {
        return mLocked;
    }

    public void setHistoryListener(HistoryListener listener) {
        this.mHistoryListener = listener;
    }

    public interface HistoryListener {
        void onHistoryTimeChanged(long time);

        void onScrolling(long time);
    }

}
