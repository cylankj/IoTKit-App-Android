package com.cylan.jiafeigou.widget.wheel;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;


/**
 * Created by cylan-com.cylan.jiafeigou.widget on 16-6-18.
 */

public class WheelView extends View {
    final static String TAG = "WheelView:";
    /**
     * marker painter
     */
    private Paint dataPaint = new Paint();
    private Paint textPaint = new Paint();
    private DataSource dataSource;
    private TouchHandler touchHandler;
    private Scroller scroller;
    private static final int MAX_COUNT = 40;
    private int colorDataBlue = Color.parseColor("#36bdff");
    private int colorDataGray = Color.parseColor("#dedede");
    private float intervalDistance;
    private float offsetLeft = 0;
    private float offsetTop = 0;
    private float itemWidth = 3;
    private float shouldScrollX = 0;
    private int dataCount = 0;
    private int textSize = 13;
    private Rect textHeightRect = new Rect();
    private int longBarTop = 0;
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
     * Indicates that the pager is in the process of settling to a final position.
     */
    public static final int SCROLL_STATE_SETTLING = 2;

    private OnItemChangedListener onItemChangedListener;

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        dataPaint.setAntiAlias(true);
        textPaint.setAntiAlias(true);
        textPaint.setColor(colorDataGray);
        textSize = convertToPx(textSize, getResources());
        textPaint.setTextSize(textSize);
        textPaint.getTextBounds("15", 0, 2, textHeightRect);
        dataSource = new DataSource();
        itemWidth = convertToPx((int) itemWidth, getResources());
        touchHandler = new TouchHandler(this);
        scroller = new Scroller(getContext());
        offsetTop = convertToPx(5, getResources());
    }

    public static int convertToPx(int dp, Resources resources) {
        DisplayMetrics dm = resources.getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    /***
     * 数据类型也有外部生成之后，才传进来。避免在view中处理数据。
     *
     * @param dataSet
     */
    public void setDataSet(DataSource dataSet) {
        this.dataSource = dataSet;
        dataCount = dataSet.dataSet.length;
        dataSource.dataStartXSet = new float[dataCount];
        updateOffset();
        invalidate();
    }

    /**
     * 数据变化，要更新整体左边的偏移量。
     */
    private void updateOffset() {
        final float halfWidth = getMeasuredWidth() / 2;
        final float dataWidth = dataCount * (itemWidth + intervalDistance);
        if (halfWidth > dataWidth) {
        } else {
        }
        Log.d(TAG, "offsetX: " + offsetLeft);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 这一天的数据类型：4种，可参考 {@link DataSource}
     *
     * @param position
     * @return
     */
    private int getItemType(final int position) {
        if (position < 0)
            return 0;
        for (int i = 0; i < dataCount; i++) {
            if (position == i)
                return dataSource.dataTypeSet[i];
        }
        return 0;
    }

    /**
     * 此数据的顶部偏移量。
     *
     * @param type
     * @return
     */
    private int getItemHeightByType(final int type) {
        switch (type) {
            case DataSource.TYPE_SHORT_INVALID:
            case DataSource.TYPE_SHORT_VALID:
                return getMeasuredHeight() / 5;
            case DataSource.TYPE_LONG_INVALID:
            case DataSource.TYPE_LONG_VALID:
                return longBarTop;
        }
        return getTop();
    }

    /**
     * 此数据对应的颜色。
     *
     * @param type
     * @return
     */
    private int getItemColorByType(final int type) {
        switch (type) {
            case DataSource.TYPE_SHORT_INVALID:
            case DataSource.TYPE_LONG_INVALID:
                return colorDataGray;
            case DataSource.TYPE_SHORT_VALID:
            case DataSource.TYPE_LONG_VALID:
                return colorDataBlue;
        }
        return Color.GRAY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        final float markerRight = getMeasuredWidth() / 2 + itemWidth / 2;
        for (int i = 0; i < dataCount; i++) {
            final float endX = markerRight -
                    (dataCount - 1 - i) * (intervalDistance + itemWidth)
                    + offsetLeft;
            final float startX = endX - itemWidth;
            dataSource.dataStartXSet[i] = startX;
            final int itemType = getItemType(i);
            if (itemType == DataSource.TYPE_LONG_INVALID || itemType == DataSource.TYPE_LONG_VALID)
                drawText(canvas, i);
            final float height = getItemHeightByType(itemType);
            final int color = getItemColorByType(itemType);
            dataPaint.setColor(color);
            canvas.drawRect(startX, (float) getBottom() - height, endX, (float) getBottom(), dataPaint);
        }
        canvas.restore();
    }

    /**
     * 画上日期
     *
     * @param canvas
     * @param position
     */
    private void drawText(final Canvas canvas, final int position) {
        if (position < 0 || position > dataCount - 1)
            return;
        final String date = dataSource.dateInStr.get(position + "");
        if (TextUtils.isEmpty(date))
            return;
        textPaint.getTextBounds(date, 0, date.length() - 1, textHeightRect);
        final int itemCenter = (int) dataSource.dataStartXSet[position] + ((int) itemWidth >> 1);
        canvas.drawText(date, itemCenter - ((int) textHeightRect.width() >> 1) - 10, textHeightRect.height(), textPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        intervalDistance = (getMeasuredWidth() - MAX_COUNT * itemWidth) / MAX_COUNT;
        longBarTop = getMeasuredHeight() * 2 / 3;
        textHeightRect.bottom = getMeasuredHeight() / 3;
    }

    public void setShouldScrollX(float shouldScrollX) {
        this.shouldScrollX = shouldScrollX;
        invalidate();
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        ViewState ss = new ViewState(superState);
        ss.shouldScrollX = shouldScrollX;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        ViewState ss = (ViewState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setShouldScrollX(ss.shouldScrollX);
    }

    /**
     * 寻找出离中心线最近的数据index,分三种情况{左边，中间，右边}，后期有待优化
     *
     * @return
     */
    private float[] computeAutoScrollDistanceX(final int direction) {
        final int scrollX = getScrollX();
        Log.d(TAG, "scrollXXXX: " + scrollX);
        final float delta = getMeasuredWidth() / 2 - itemWidth / 2;
        final float markLabelStartX = delta + scrollX;
        float[] returnValue = new float[2];
        if (scrollX > 0) {
            for (int i = dataCount - 1; i >= 0; i--) {
                if (dataSource.dataTypeSet[i] == DataSource.TYPE_SHORT_VALID || dataSource.dataTypeSet[i] == DataSource.TYPE_LONG_VALID) {
                    returnValue[0] = i;
                    returnValue[1] =
                            -Math.abs(Math.abs(dataSource.dataStartXSet[i]
                                    - dataSource.dataStartXSet[dataCount - 1]));
                    return returnValue;
                }
            }
        }
        if (markLabelStartX <= dataSource.dataStartXSet[0]) {
            for (int i = 0; i < dataCount; i++) {
                if (dataSource.dataTypeSet[i] == DataSource.TYPE_SHORT_VALID || dataSource.dataTypeSet[i] == DataSource.TYPE_LONG_VALID) {
                    returnValue[0] = i;
                    returnValue[1] =
                            -Math.abs(dataSource.dataStartXSet[i]
                                    - dataSource.dataStartXSet[dataCount - 1]);
                    return returnValue;
                }
            }
        }
        for (int i = 1; i < dataCount; i++) {
            if (dataSource.dataStartXSet[i] > markLabelStartX
                    && dataSource.dataStartXSet[i - 1] <= markLabelStartX) {
                int leftPos = -1;
                for (int left = i - 1; left >= 0; left--) {
                    if (dataSource.dataTypeSet[left] == DataSource.TYPE_SHORT_VALID
                            || dataSource.dataTypeSet[left] == DataSource.TYPE_LONG_VALID) {
                        leftPos = left;
                        break;
                    }
                }
                if (leftPos == -1) {
                    throw new IllegalArgumentException("数据出错，");
                }
                int rightPos = -1;
                for (int right = i; right < dataCount; right++) {
                    if (dataSource.dataTypeSet[right] == DataSource.TYPE_SHORT_VALID
                            || dataSource.dataTypeSet[right] == DataSource.TYPE_LONG_VALID) {
                        rightPos = right;
                        break;
                    }
                }
                if (rightPos == -1) {
                    throw new IllegalArgumentException("数据出错，");
                }
                final float deltaLeft = Math.abs(Math.abs(dataSource.dataStartXSet[leftPos]
                        - dataSource.dataStartXSet[dataCount - 1])
                        - Math.abs(scrollX));
                final float deltaRight = Math.abs(
                        Math.abs(scrollX) - Math.abs(dataSource.dataStartXSet[dataCount - 1]
                                - dataSource.dataStartXSet[rightPos]));
                if (direction == 0) {
                    //left
                    returnValue[0] = rightPos;
                    returnValue[1] =
                            -Math.abs(dataSource.dataStartXSet[dataCount - 1]
                                    - dataSource.dataStartXSet[rightPos]);
                    return returnValue;
                } else if (direction == 1) {
                    returnValue[0] = leftPos;
                    returnValue[1] =
                            -(Math.abs(scrollX) + Math.abs(deltaLeft));
                    return returnValue;
                }
                if (deltaLeft >= deltaRight) {
                    returnValue[0] = rightPos;
                    returnValue[1] =
                            -Math.abs(dataSource.dataStartXSet[dataCount - 1]
                                    - dataSource.dataStartXSet[rightPos]);
                    return returnValue;
                } else {
                    returnValue[0] = leftPos;
                    returnValue[1] =
                            -(Math.abs(scrollX) + Math.abs(deltaLeft));
                    return returnValue;
                }
            }
        }
        return returnValue;
    }

    /**
     * 自动修正位置
     */
    public void autoSettle(final String tag, final int direction) {
        float[] data =
                computeAutoScrollDistanceX(direction);
        shouldScrollX = data[1];
        int position = (int) data[0];
        final int scrollX = getScrollX();
        scroller.startScroll(getScrollX(), 0, (int) shouldScrollX - scrollX, 0);
        Log.d(TAG, "autoSettle....." + scrollX + "  .. " + position);
        postInvalidate();
        if (onItemChangedListener != null)
            onItemChangedListener.onItemChanged(position, dataSource.dateInStr.get("" + position));
    }

    public boolean onTouchEvent(MotionEvent event) {
        return touchHandler.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
            Log.d(TAG, "computeScroll");
        }
        super.computeScroll();
    }

    /**
     * 参考 progressBar ,可当成一个模板使用。
     */
    public static class ViewState extends View.BaseSavedState {


        float shouldScrollX;
        int position;

        public ViewState(Parcel source) {
            super(source);
            shouldScrollX = source.readFloat();
        }

        public ViewState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(shouldScrollX);
        }

        public static final Parcelable.Creator<ViewState> CREATOR
                = new Parcelable.Creator<ViewState>() {
            public ViewState createFromParcel(Parcel in) {
                return new ViewState(in);
            }

            public ViewState[] newArray(int size) {
                return new ViewState[size];
            }
        };
    }

    public void setOnItemChangedListener(OnItemChangedListener onItemChangedListener) {
        this.onItemChangedListener = onItemChangedListener;
    }

    public interface OnItemChangedListener {
        void onItemChanged(final int position, final String dateInStr);
    }

}



