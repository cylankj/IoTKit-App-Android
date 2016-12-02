package com.cylan.jiafeigou.widget.wheel;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.support.v4.util.LongSparseArray;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.PopupWindow;
import android.widget.Scroller;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.tencent.bugly.crashreport.inner.InnerAPI.context;

/*
 *  @项目名：  TimeLinePicker 
 *  @包名：    com.yzd.timelinepicker
 *  @文件名:   TimeLinePicker
 *  @创建者:   yanzhendong
 *  @创建时间:  2016/11/5 12:24
 *  @描述：    TODO
 */
@SuppressWarnings("unused")
public class TimeWheelView extends View implements ValueAnimator.AnimatorUpdateListener {
    private static final String TAG = "TimeLinePicker";
    private int mViewWidth;//控件的宽度
    private int mViewHeight;//控件的高度
    private Paint mDividePaint;//中间的刻度线的画笔
    private Paint mMarkLineBGPaint;//背景刻度线画笔
    private Paint mMarkLinePaint;//有数据的刻度线的画笔
    private Paint mMonthLinePaint;//有数据的月的刻度线的画笔
    private Paint mMonthTextPaint;//有数据且为month的刻度线的画笔
    private Paint mBoundLinePaint;
    private int mMarkLineSpace;//刻度线之间的间距
    private int mMiddleX;//中间的刻度线的X坐标
    private int mMarkLineHeight;//天刻度线的高度
    private int mMonthLineHeight;//月刻度线的高度
    private int mMonthTextMargin;//文字距离刻度线的margin
    private int mDivideLineColor;//中间的刻度线的颜色
    private int mMarkLineColorBG;//刻度线的背景色，即没有数据时的颜色
    private int mMarkLineColor;//天的刻度线有数据时的颜色
    private int mMonthTextColor;//刻度线上面的日期文字颜色
    private int mMonthLineColor;//月的刻度线的颜色
    private int mMarkLineWidth;//天的刻度线的宽度
    private int mDivideLineWidth;//中间的刻度线的宽度
    private int mMonthLineWidth;//月的刻度线的宽度
    private float mMonthTextSize;//popWindow中文字的大小
    private int mAnimatorDuration;//对齐动画的持续时间
    private int mPopWindowLayout;//popWindow中view的布局文件,该布局文件中必须有一个tag为"contentView"的TextView
    private int mPopWindowMargin;//popWindow距离下面刻度线的间距
    private int mBoundLineWidth;
    private int mBoundLineColor;
    private float mDivideLineHeight;
    private String mMonthFont;

    private int mDrawOffsetX;//Touch事件造成的偏移量
    private LongSparseArray<TimePair> mTimeLineMap = new LongSparseArray<>(512);//以天为单位排序后的map集合,是有序的
    private final long DAY_MILLISECOND = 1000 * 60 * 60 * 24;//一天的毫秒数
    private final long INVALID_DAY_INDEX = -1;
    private long mDayIndex = INVALID_DAY_INDEX;//当前显示天的index
    private long mMaxDayIndex = INVALID_DAY_INDEX;//最小显示天的index
    private long mMinDayIndex = INVALID_DAY_INDEX;//最大显示天的index
    private int mLastTouchX;//touch事件中上一次的X坐标
    private int mLastMovingX;//fling事件中的上一次X坐标
    private final Calendar mCalendar = Calendar.getInstance(Locale.CANADA);
    private final Calendar mTempCalendar = Calendar.getInstance(Locale.CANADA);
    private int mDayCount;//当月的天数
    private ValueAnimator mAlignAnimator;
    private List<OnTimeLineChangeListener> mTimeLineListeners = new ArrayList<>();
    private PopupWindow mTimeLinePopWindow;
    private long mPopWindowShowTime = 3000;//PopWindow在松手三秒以后消失
    private TimeInterpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private boolean mTimeLineChanged;
    private boolean mHasHideAction = false;
    private static final int TIME_LINE_FLAG_NEXT = 1;
    private static final int TIME_LINE_FLAG_PREV = 2;
    private static final int TIME_LINE_FLAG_TODAY_PREV = 3;
    private static final int TIME_LINE_FLAG_TODAY = 4;
    private static final int TIME_LINE_FLAG_TODAY_NEXT = 5;
    private int mTimeLineFlag = TIME_LINE_FLAG_TODAY;
    private TextView mContentView;

    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;
    private boolean mHasFlingAction;
    private int mMinFlingX;
    private int mMaxFlingX;
    private int mFlingDistance;
    private long mAnimateFinalDayIndex;
    private boolean mHidePopWindow = false;
    private long mLastDayIndex;
    private boolean mAlwaysEndWithData = true;

    private static final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
    private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
    private float mFlingFriction = ViewConfiguration.getScrollFriction();
    private float mPhysicalCoeff;
    private float mPpi;
    private boolean mHasInit = false;
    private boolean mNotNotify = false;


    public TimeWheelView(Context context) {
        this(context, null);
    }

    public TimeWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TimeWheelView);
        mDivideLineHeight = array.getDimension(R.styleable.TimeWheelView_divideLine_height, dp2px(context, 49));
        mMarkLineColor = array.getColor(R.styleable.TimeWheelView_markLine_color, 0XFF36BDFF);
        mMarkLineColorBG = array.getColor(R.styleable.TimeWheelView_markLine_color_bg, 0XFFDEDEDE);
        mMarkLineWidth = array.getDimensionPixelSize(R.styleable.TimeWheelView_markLine_width, dp2px(context, 2F));
        mMonthTextColor = array.getColor(R.styleable.TimeWheelView_monthText_color, 0XFF888888);
        mMonthTextSize = array.getDimension(R.styleable.TimeWheelView_monthText_size, dp2px(context, 11F));
        mMonthLineColor = array.getColor(R.styleable.TimeWheelView_monthLine_color, 0XFF36BDFF);
        mMonthLineWidth = array.getDimensionPixelSize(R.styleable.TimeWheelView_monthLine_width, dp2px(context, 2F));
        mDivideLineColor = array.getColor(R.styleable.TimeWheelView_divideLine_color, 0XFF36BDFF);
        mDivideLineWidth = array.getDimensionPixelSize(R.styleable.TimeWheelView_divideLine_width, dp2px(context, 2F));
        mPopWindowShowTime = array.getInt(R.styleable.TimeWheelView_popWindow_showTime, 2000);
        mAnimatorDuration = array.getInt(R.styleable.TimeWheelView_animate_duration, 200);
        mPopWindowLayout = array.getResourceId(R.styleable.TimeWheelView_popWindow_layout, R.layout.view_time_line_pop);
        mPopWindowMargin = array.getDimensionPixelSize(R.styleable.TimeWheelView_popWindow_margin, dp2px(context, 5F));
        mMarkLineSpace = array.getDimensionPixelSize(R.styleable.TimeWheelView_markLine_space, dp2px(context, 7));
        mMarkLineHeight = array.getDimensionPixelSize(R.styleable.TimeWheelView_markLine_height, dp2px(context, 9));
        mMonthLineHeight = array.getDimensionPixelSize(R.styleable.TimeWheelView_monthLine_height, dp2px(context, 17));
        mMonthTextMargin = array.getDimensionPixelSize(R.styleable.TimeWheelView_monthText_margin, dp2px(context, 14F));
        mBoundLineWidth = (int) array.getDimension(R.styleable.TimeWheelView_bound_line_width, dp2px(context, 2F));
        mBoundLineColor = array.getColor(R.styleable.TimeWheelView_bound_line_color, 0XFFD2D2D2);
        mMonthFont = array.getString(R.styleable.TimeWheelView_monthText_font);
        array.recycle();
        init();
    }

    private void init() {
        mDividePaint = new TextPaint();
        mDividePaint.setColor(mDivideLineColor);
        mDividePaint.setStrokeWidth(mDivideLineWidth);
        mDividePaint.setAntiAlias(true);

        mMarkLineBGPaint = new TextPaint();
        mMarkLineBGPaint.setColor(mMarkLineColorBG);
        mMarkLineBGPaint.setStrokeWidth(mMarkLineWidth);
        mMarkLineBGPaint.setAntiAlias(true);

        mMarkLinePaint = new TextPaint();
        mMarkLinePaint.setColor(mMarkLineColor);
        mMarkLinePaint.setStrokeWidth(mMarkLineWidth);
        mMarkLinePaint.setAntiAlias(true);

        mMonthLinePaint = new TextPaint();
        mMonthLinePaint.setColor(mMonthLineColor);
        mMonthLinePaint.setStrokeWidth(mMonthLineWidth);
        mMonthLinePaint.setAntiAlias(true);

        mMonthTextPaint = new TextPaint();
        mMonthTextPaint.setColor(mMonthTextColor);
        mMonthTextPaint.setTextSize(mMonthTextSize);
        mMonthTextPaint.setStyle(Paint.Style.STROKE);
        mMonthTextPaint.setAntiAlias(true);
        mMonthTextPaint.setTextAlign(Paint.Align.CENTER);
        mMonthTextPaint.setTypeface(Typeface.create(mMonthFont, Typeface.NORMAL));

        mBoundLinePaint = new TextPaint();
        mBoundLinePaint.setColor(mBoundLineColor);
        mBoundLinePaint.setStrokeWidth(mBoundLineWidth);
        mBoundLinePaint.setAntiAlias(true);

        mAlignAnimator = new ValueAnimator();
        mAlignAnimator.setInterpolator(mInterpolator);
        mAlignAnimator.setDuration(mAnimatorDuration);
        mAlignAnimator.addUpdateListener(this);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
        mVelocityTracker = VelocityTracker.obtain();
        mScroller = new Scroller(getContext());
        mPhysicalCoeff = computeDeceleration(0.84f); // look and feel tuning

        final View view = LayoutInflater.from(getContext()).inflate(mPopWindowLayout, null);
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        mContentView = (TextView) view.findViewWithTag("contentView");
        mTimeLinePopWindow = new PopupWindow(view, view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mViewWidth = w;
        this.mViewHeight = h;
        if (mMarkLineSpace == 0) {
            mMarkLineSpace = mViewWidth / 40;
        }
        if (mMarkLineHeight == 0) {
            mMarkLineHeight = mViewHeight / 4;
        }
        if (mMonthLineHeight == 0) {
            mMonthLineHeight = mViewHeight / 2;
        }
        mMiddleX = mViewWidth / 2;
        mFlingDistance = mViewWidth * 2;
        mHasInit = true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mDayIndex != INVALID_DAY_INDEX) {
            //画下面的小刻度线
            long dayIndex;
            TimePair timePair;
            for (int dayPos = 0; dayPos < 40; dayPos++) {

                //画中间线左边的刻度线
                dayIndex = mDayIndex - dayPos;
                if (dayIndex >= mMinDayIndex && dayIndex <= mMaxDayIndex) {
                    float drawX = mMiddleX - dayPos * mMarkLineSpace + mDrawOffsetX;
                    timePair = mTimeLineMap.get(dayIndex);
                    int day = mCalendar.get(Calendar.DAY_OF_MONTH) - dayPos;
                    boolean preMonth = false;
                    if (day < 0) {//说明是在上一个月了
                        mTempCalendar.setTimeInMillis(mCalendar.getTimeInMillis());
                        mTempCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) - 1);
                        int days = mTempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                        day = days + day;
                        preMonth = true;
                    }
                    boolean isMonth = day == 1 || day == 15;
                    if (isMonth) {
                        int month = mCalendar.get(Calendar.MONTH) + 1 + (preMonth ? -1 : 0);
                        String text = day == 15 ? month + "-15" : month + "月";
                        canvas.drawText(text, drawX, mViewHeight - mMonthLineHeight - mMonthTextMargin, mMonthTextPaint);
                    }
                    if (timePair != null) {//表示这一天有数据
                        canvas.drawLine(drawX, isMonth ? mViewHeight - mMonthLineHeight : mViewHeight - mMarkLineHeight, drawX, mViewHeight, mMarkLinePaint);
                    } else {//表示这一天没有数据
                        canvas.drawLine(drawX, isMonth ? mViewHeight - mMonthLineHeight : mViewHeight - mMarkLineHeight, drawX, mViewHeight, mMarkLineBGPaint);
                    }
                }

                //画中间线右边的刻度线
                dayIndex = mDayIndex + dayPos;
                if (dayIndex >= mMinDayIndex && dayIndex <= mMaxDayIndex) {
                    float drawX = mMiddleX + dayPos * mMarkLineSpace + mDrawOffsetX;
                    timePair = mTimeLineMap.get(dayIndex);
                    int day = mCalendar.get(Calendar.DAY_OF_MONTH) + dayPos;
                    boolean isMonth = day - mDayCount == 1 || day == 15 || day - mDayCount == 15;
                    boolean nextMonth = day - mDayCount == 15;
                    if (isMonth) {
                        int month = mCalendar.get(Calendar.MONTH) + 1 + (nextMonth ? 1 : 0);
                        String text = day == 15 || day - mDayCount == 15 ? month + "-15" : ((month + 1) == 13 ? 1 : (month + 1)) + "月";
                        canvas.drawText(text, drawX, mViewHeight - mMonthLineHeight - mMonthTextMargin, mMonthTextPaint);
                    }
                    if (timePair != null) {//表示这一天有数据
                        canvas.drawLine(drawX, isMonth ? mViewHeight - mMonthLineHeight : mViewHeight - mMarkLineHeight, drawX, mViewHeight, mMarkLinePaint);
                    } else {//表示这一天没有数据
                        canvas.drawLine(drawX, isMonth ? mViewHeight - mMonthLineHeight : mViewHeight - mMarkLineHeight, drawX, mViewHeight, mMarkLineBGPaint);
                    }
                }
            }

            if (mTimeLineChanged) {
                updateTimeLinePopWindow();
            }
        }

        //画中间的刻度线
        canvas.drawLine(mMiddleX, mViewHeight - mDivideLineHeight, mMiddleX, mViewHeight, mDividePaint);
        //画顶部的横线
        canvas.drawLine(0, 0, mViewWidth, 0, mBoundLinePaint);
    }

    private String getMonthText(boolean halfMonth, int month) {
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHasFlingAction = false;//当产生ActionDown事件时停止正在进行的fling事件
                mLastTouchX = (int) event.getX();
                mLastDayIndex = mDayIndex;
                showTimeLinePopWindow();
                break;
            case MotionEvent.ACTION_MOVE:
                int index = event.getActionIndex();
                if (mHasHideAction) {
                    removeCallbacks(mHidePopWindowCallback);
                    mHasHideAction = false;
                }
                int newX = (int) event.getX();
                //当move速度过快达到fling的速度时不通知观察者日期改变
//                mLastTouchX = calcDayIndex(newX, mLastTouchX, !isFling());
                mLastTouchX = calcDayIndex(newX, mLastTouchX, false);
                break;
            case MotionEvent.ACTION_UP:
                doFlingAction(event);
                if (!mHasFlingAction) {
                    long distance = mDayIndex - mLastDayIndex;
                    Log.e(TAG, "onTouchEvent: " + distance);
                    if (mAlwaysEndWithData && distance != 0) {
                        long dayIndex;
                        if (distance < 0) {
                            dayIndex = findSuitableDayIndex(mDayIndex, true);
                        } else {
                            dayIndex = findSuitableDayIndex(mDayIndex, false);
                        }
                        Log.e(TAG, "onTouchEvent: dayIndex" + dayIndex);
                        animateToDayIndex(dayIndex);
                    } else {
                        animateTimeLine(TIME_LINE_FLAG_TODAY);
                        hideTimeLinePopWindow(mPopWindowShowTime);
                    }
                }
                break;
        }
        return true;
    }

    private boolean isFling() {
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
        final float velocityX = mVelocityTracker.getXVelocity();
        return Math.abs(velocityX) > mMinimumFlingVelocity;
    }

    private void doFlingAction(MotionEvent event) {
        // A fling must travel the minimum tap distance
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
        final float velocityX = mVelocityTracker.getXVelocity();
        final float velocityY = mVelocityTracker.getYVelocity();
        if (Math.abs(velocityX) > mMinimumFlingVelocity) {
            /*产生了fling事件,但此时要判断是否可以进行fling,
             *当mDayIndex==mMinDayIndex或mDayIndex==mMaxDayIndex时要忽略掉该fling事件*/
            if ((velocityX > 0 && mDayIndex > mMinDayIndex) || (velocityX < 0 && mDayIndex < mMaxDayIndex)) {
                mHasFlingAction = true;
//                mScroller.fling(0, 0, (int) velocityX, 0, -mFlingDistance, mFlingDistance, 0, 0);

                double totalDistance = getSplineFlingDistance(velocityY);
                float coeffX = velocityY == 0 ? 1.0f : velocityX / velocityY;
                int finalX = (int) Math.round(totalDistance * coeffX);
                long flingIndex = mDayIndex - finalX / mMarkLineSpace;
                long dayIndex = findSuitableDayIndex(flingIndex, velocityX > 0);
                mScroller.startScroll(0, 0, (int) ((mDayIndex - dayIndex) * mMarkLineSpace), 0);
                invalidate();
            }
        }
        mVelocityTracker.clear();
    }

    private int getSplineFlingDuration(float velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return (int) (1000.0 * Math.exp(l / decelMinusOne));
    }

    private float computeDeceleration(float friction) {
        return SensorManager.GRAVITY_EARTH   // g (m/s^2)
                * 39.37f               // inch/meter
                * mPpi                 // pixels per inch
                * friction;
    }

    private double getSplineFlingDistance(float velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l);
    }

    private double getSplineDeceleration(float velocity) {
        return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * mPhysicalCoeff));
    }

    public void append(List<Long> elements) {
        addAll(elements, false);
    }

    public void insert(long time) {
        long dayIndex = time / DAY_MILLISECOND;
        TimePair eles = mTimeLineMap.get(dayIndex);
        if (eles == null) eles = new TimePair();
        eles.timeCount++;
        if (time < eles.minLongTime) eles.minLongTime = time;
        mTimeLineMap.put(dayIndex, eles);
        notifyDataSetChanged();
    }

    public boolean delete(long time) {
        return delete(time, false);
    }

    public boolean forceDelete(long time) {
        return delete(time, true);
    }

    public boolean moveToDay(long time) {
        long dayIndex = time / DAY_MILLISECOND;
        return animateToDayIndex(dayIndex);
    }

    public boolean updateDay(long time) {
        long dayIndex = time / DAY_MILLISECOND;
        mNotNotify = true;
        return animateToDayIndex(dayIndex);
    }

    @Override
    public void computeScroll() {
        if (!mHasFlingAction && mScroller.isFinished()) {
            return;
        }
        if (mScroller.computeScrollOffset() && mHasFlingAction && Math.abs(mDrawOffsetX) < mViewWidth / 3) {
            int newX = mScroller.getCurrX();
            mLastMovingX = calcDayIndex(newX, mLastMovingX, false);
        } else {
            mLastMovingX = 0;
            mHasFlingAction = false;
            mScroller.abortAnimation();
            animateTimeLine(TIME_LINE_FLAG_TODAY);
            hideTimeLinePopWindow(mPopWindowShowTime);
        }
    }

    private int calcDayIndex(int newX, int lastX, boolean notify) {
        int dayPos = (newX - lastX) / mMarkLineSpace;
        if (dayPos != 0 && mDayIndex - dayPos >= mMinDayIndex && mDayIndex - dayPos <= mMaxDayIndex) {
            mDayIndex -= dayPos;
            if (mTimeLineMap.get(mDayIndex) != null) mLastDayIndex = mDayIndex;
            mCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.get(Calendar.DAY_OF_MONTH) - dayPos);
            mDayCount = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            lastX += dayPos * mMarkLineSpace;
//            mTimeLineChanged = true;
            if (notify) {
                TimePair pair = mTimeLineMap.get(mDayIndex);
                if (pair != null) {
                    long newTime = pair.minLongTime;
                    //这里判断这一天是否有数据,有数据再通知更新,否则不更新
                    if (mTimeLineMap.get(newTime / DAY_MILLISECOND) != null) {
                        for (OnTimeLineChangeListener listener : mTimeLineListeners) {
                            listener.onTimeLineChanged(newTime);
                        }
                    }
                }
            }
        }
        mDrawOffsetX = newX - lastX;
        invalidate();
        return lastX;
    }

    public void showTimeLinePopWindow() {
        if (!mTimeLinePopWindow.isShowing()) {
            mTimeLineChanged = true;
            mTimeLinePopWindow.showAsDropDown(this, mMiddleX - mTimeLinePopWindow.getWidth() / 2, -mTimeLinePopWindow.getHeight() - mViewHeight - mPopWindowMargin);
        }
        if (mHasHideAction) {
            removeCallbacks(mHidePopWindowCallback);
            mHasHideAction = false;
        }
        updateTimeLinePopWindow();
    }

    public void show() {

    }

    public void hide() {

    }

    private void updateTimeLinePopWindow() {
        mTempCalendar.setTime(mCalendar.getTime());
        if (mTimeLineFlag == TIME_LINE_FLAG_PREV) {
            mTempCalendar.set(Calendar.DAY_OF_MONTH, mTempCalendar.get(Calendar.DAY_OF_MONTH) - 1);
        } else if (mTimeLineFlag == TIME_LINE_FLAG_NEXT) {
            mTempCalendar.set(Calendar.DAY_OF_MONTH, mTempCalendar.get(Calendar.DAY_OF_MONTH) + 1);
        }
        String text = (mTempCalendar.get(Calendar.YEAR)) + "-" + (mTempCalendar.get(Calendar.MONTH) + 1) + "-" + mTempCalendar.get(Calendar.DAY_OF_MONTH);
        mContentView.setText(text);
        mTimeLineChanged = false;
    }


    private Runnable mHidePopWindowCallback = new Runnable() {
        @Override
        public void run() {
            mTimeLinePopWindow.dismiss();
            mHasHideAction = false;
        }
    };

    public void hideTimeLinePopWindow() {
        hideTimeLinePopWindow(0);
    }

    private void hideTimeLinePopWindow(long delay) {
        if (mHasHideAction) {
            removeCallbacks(mHidePopWindowCallback);
        }

        //如果不需要隐藏popWindow则不进行隐藏
        if (mHidePopWindow || delay == 0) {
            postDelayed(mHidePopWindowCallback, delay);
            mHasHideAction = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTimeLinePopWindow.dismiss();
    }

    private boolean animateToDayIndex(long dayIndex, int duration) {
        if (dayIndex > mMaxDayIndex || dayIndex < mMinDayIndex || mAlignAnimator.isRunning() || !mHasInit || !mTimeLinePopWindow.isShowing()) {
            return false;
        }
//        showTimeLinePopWindow();
        int days = (int) Math.abs(dayIndex - mDayIndex);
        if ((mDayIndex == mMaxDayIndex && mDrawOffsetX < 0) || (mDayIndex == mMinDayIndex && mDrawOffsetX > 0) || (
                Math.abs(mDrawOffsetX) < mMarkLineSpace / 2 && mDrawOffsetX != 0 && !mAlwaysEndWithData)) {
            mAlignAnimator.setIntValues(mDrawOffsetX, 0);
        } else {
            days = Math.abs(mDrawOffsetX) > mMarkLineSpace / 2 && !mAlwaysEndWithData ? days + 1 : days;
            int endX = mDrawOffsetX > 0 || dayIndex < mDayIndex ? mMarkLineSpace * days : -mMarkLineSpace * days;
            mAlignAnimator.setIntValues(mDrawOffsetX, endX);
        }
        mLastMovingX = 0;
        mAlignAnimator.setDuration(duration);
        mAlignAnimator.start();
        return true;
    }

    private boolean animateToDayIndex(long dayIndex) {
        return animateToDayIndex(dayIndex, mAnimatorDuration);
    }

    public boolean setCurrentDay(long time) {
        long dayIndex = time / DAY_MILLISECOND;
        boolean success;
        if (dayIndex < mMinDayIndex || dayIndex > mMaxDayIndex) {
            success = false;
        } else {
            success = true;
            mDayIndex = dayIndex;
            mLastDayIndex = mDayIndex;
            mCalendar.setTimeInMillis(time);
            mDayCount = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        return success;
    }

    private long findSuitableDayIndex(long refDayIndex, boolean backward) {
        if (refDayIndex > mMaxDayIndex) return mMaxDayIndex;
        if (refDayIndex < mMinDayIndex) return mMinDayIndex;
        TimePair timePair = mTimeLineMap.get(refDayIndex);
        boolean remove = timePair == null;
        if (timePair == null) mTimeLineMap.put(refDayIndex, timePair);
        int dayIndex = mTimeLineMap.indexOfKey(refDayIndex);
        if (backward) {
            dayIndex--;
        } else {
            dayIndex++;
        }
        long result;
        if (dayIndex < 0 || dayIndex > mTimeLineMap.size() - 1) {
            result = refDayIndex;
        } else {
            result = mTimeLineMap.keyAt(dayIndex);
        }
        if (remove) mTimeLineMap.remove(refDayIndex);
        return result;
    }

    public void moveToHead() {
        moveToDay(mTimeLineMap.keyAt(mTimeLineMap.size() - 1) * DAY_MILLISECOND);
    }

    public void moveToFoot() {
        moveToDay(mTimeLineMap.keyAt(0) * DAY_MILLISECOND);
    }

    private boolean animateTimeLine(int flag) {
        mTimeLineFlag = flag;
        int dayIndex = (int) mDayIndex;
        if (mTimeLineFlag == TIME_LINE_FLAG_NEXT) {
            dayIndex++;
        } else if (mTimeLineFlag == TIME_LINE_FLAG_PREV) {
            dayIndex--;
        }
        return animateToDayIndex(dayIndex);
    }

    public void setTimeLineElements(List<Long> elements) {
        addAll(elements, true);
    }

    private void addAll(List<Long> elements, boolean clear) {
        if (clear) {
            mDayIndex = -1;
            mTimeLineMap.clear();
        }
        TimePair eles;
        for (Long element : elements) {
            long dayIndex = element / DAY_MILLISECOND;
            eles = mTimeLineMap.get(dayIndex);
            if (eles == null) eles = new TimePair();
            eles.timeCount++;
            if (element < eles.minLongTime) eles.minLongTime = element;
            mTimeLineMap.put(dayIndex, eles);
        }
        notifyDataSetChanged();
    }

    private boolean delete(long time, boolean force) {
        boolean success;
        long dayIndex = time / DAY_MILLISECOND;
        TimePair eles = mTimeLineMap.get(dayIndex);
        if (eles == null) {
            success = false;
        } else if (force) {
            success = true;
            mTimeLineMap.remove(dayIndex);
        } else if (--eles.timeCount == 0) {
            success = true;
            mTimeLineMap.remove(dayIndex);
        } else {
            success = false;
            mTimeLineMap.put(dayIndex, eles);
        }
        notifyDataSetChanged();
        return success;
    }

    public void notifyDataSetChanged() {
        if (mTimeLineMap.size() == 0) {
            mDayIndex = INVALID_DAY_INDEX;
            mMaxDayIndex = INVALID_DAY_INDEX;
            mMinDayIndex = INVALID_DAY_INDEX;
        } else {
            mMaxDayIndex = mTimeLineMap.keyAt(mTimeLineMap.size() - 1);//集合最大值
            mMinDayIndex = mTimeLineMap.keyAt(0);//集合最小值
            if (mDayIndex == INVALID_DAY_INDEX) {
                mDayIndex = mMaxDayIndex;//给mDayIndex赋默认值
                mCalendar.setTimeInMillis(mDayIndex * DAY_MILLISECOND);
                mDayCount = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            } else if (mDayIndex > mMaxDayIndex || mDayIndex < mMinDayIndex) {
                animateToDayIndex(mDayIndex > mMaxDayIndex ? mMaxDayIndex : mMinDayIndex);
            }
        }
        mTimeLineChanged = true;
        invalidate();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int newX = (int) animation.getAnimatedValue();
        boolean finish = animation.getAnimatedFraction() == 1.0F;
        mLastMovingX = calcDayIndex(newX, mLastMovingX, finish && !mNotNotify);
        if (finish) {
            mLastMovingX = 0;
            mDrawOffsetX = 0;
            mTimeLineFlag = TIME_LINE_FLAG_TODAY;
            mNotNotify = false;
            mTimeLineChanged = true;
            hideTimeLinePopWindow(mPopWindowShowTime);
        }
    }

    public void addTimeLineListener(OnTimeLineChangeListener listener) {
        if (!mTimeLineListeners.contains(listener)) {
            mTimeLineListeners.add(listener);
        }
    }

    public void removeTimeLineListener(OnTimeLineChangeListener listener) {
        if (mTimeLineListeners.contains(listener)) {
            mTimeLineListeners.remove(listener);
        }
    }

    public boolean moveToNext() {
        boolean success = false;
        if (mDayIndex < mMaxDayIndex) {
//            success = animateTimeLine(TIME_LINE_FLAG_NEXT);
            mTimeLineFlag = TIME_LINE_FLAG_NEXT;
            success = animateToDayIndex(findSuitableDayIndex(mDayIndex, false));
        }
        return success;
    }

    public boolean moveToPrevious() {
        boolean success = false;
        if (mDayIndex > mMinDayIndex) {
//            success = animateTimeLine(TIME_LINE_FLAG_PREV);
            mTimeLineFlag = TIME_LINE_FLAG_PREV;
            success = animateToDayIndex(findSuitableDayIndex(mDayIndex, true));
        }
        return success;
    }

    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private class TimePair {
        long minLongTime = Long.MAX_VALUE;
        int timeCount = 0;
    }

    public interface OnTimeLineChangeListener {
        void onTimeLineChanged(long newTime);
    }

    public void setMarkLineSpace(int markLineSpace) {
        mMarkLineSpace = markLineSpace;
        invalidate();
    }

    public void setMarkLineHeight(int markLineHeight) {
        mMarkLineHeight = markLineHeight;
        invalidate();
    }

    public void setMonthLineHeight(int monthLineHeight) {
        mMonthLineHeight = monthLineHeight;
        invalidate();
    }

    public void setMonthTextMargin(int monthTextMargin) {
        mMonthTextMargin = monthTextMargin;
        invalidate();
    }

    public void setDivideLineColor(int divideLineColor) {
        mDivideLineColor = divideLineColor;
        mDividePaint.setColor(mDivideLineColor);
        invalidate();
    }

    public void setMarkLineColorBG(int markLineColorBG) {
        mMarkLineColorBG = markLineColorBG;
        mMarkLineBGPaint.setColor(mMarkLineColorBG);
        invalidate();
    }

    public void setMarkLineColor(int markLineColor) {
        mMarkLineColor = markLineColor;
        mMarkLinePaint.setColor(mMarkLineColor);
        invalidate();
    }

    public void setMonthTextColor(int monthTextColor) {
        mMonthTextColor = monthTextColor;
        mMonthTextPaint.setColor(mMonthTextColor);
        invalidate();
    }

    public void setMonthLineColor(int monthLineColor) {
        mMonthLineColor = monthLineColor;
        mMonthLinePaint.setColor(mMonthLineColor);
        invalidate();
    }

    public void setMarkLineWidth(int markLineWidth) {
        mMarkLineWidth = markLineWidth;
        mMarkLinePaint.setStrokeWidth(mMarkLineWidth);
        invalidate();
    }

    public void setDivideLineWidth(int divideLineWidth) {
        mDivideLineWidth = divideLineWidth;
        mDividePaint.setStrokeWidth(mDivideLineWidth);
        invalidate();
    }

    public void setMonthLineWidth(int monthLineWidth) {
        mMonthLineWidth = monthLineWidth;
        mMonthLinePaint.setStrokeWidth(mMonthLineWidth);
        invalidate();
    }

    public void setMonthTextSize(float monthTextSize) {
        mMonthTextSize = monthTextSize;
        mMonthTextPaint.setTextSize(mMonthTextSize);
        invalidate();
    }

    public void setAnimatorDuration(int animatorDuration) {
        mAnimatorDuration = animatorDuration;
        invalidate();
    }

    /**
     * 必须要有一个tag为contentView的TextView,是根据这个tag去寻找view的
     */
    public void setPopWindowLayout(int popWindowLayout) {
        mPopWindowLayout = popWindowLayout;
        View view = View.inflate(getContext(), mPopWindowLayout, null);
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        mContentView = (TextView) view.findViewWithTag("contentView");
        mTimeLinePopWindow.setContentView(view);
        mTimeLinePopWindow.setWidth(view.getMeasuredWidth());
        mTimeLinePopWindow.setHeight(view.getMeasuredHeight());
    }

    public void setPopWindowMargin(int popWindowMargin) {
        mPopWindowMargin = popWindowMargin;
    }

    public void setPopWindowShowTime(long popWindowShowTime) {
        mPopWindowShowTime = popWindowShowTime;
    }
}
