package com.cylan.jiafeigou.widget.wheel;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.List;


/**
 * Created by yzd on 17-1-17.
 */

public class WonderIndicatorWheelView extends LinearLayout implements OnItemClickListener, ValueAnimator.AnimatorUpdateListener {

    private TextView mTitle;
    private RecyclerView mIndicatorList;
    private OnDayChangedListener mListener;
    private int mLastPosition = -1;
    private SuperAdapter<WheelItem> mAdapter;
    private WheelLayoutManager mManager;
    private ValueAnimator mAnimator;
    private FrameLayout mWonderItemsContainer;
    private float mFactor = 2.0F;
    private int mViewWidth;
    private static final long DAY_TIME = 24 * 60 * 60 * 1000L;

    public WonderIndicatorWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.wonder_time_indicator, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle = (TextView) findViewById(R.id.wonder_item_title);
        mIndicatorList = (RecyclerView) findViewById(R.id.wonder_item_indicator_list);
        mWonderItemsContainer = (FrameLayout) findViewById(R.id.wonder_indicator_container);
        initView();
    }

    private int mRestoreX = 0;
    private int mMaxPullDistance;

    public float getInterpolation(float input) {
        float result;
        if (mFactor == 1.0f) {
            result = 1.0f - (1.0f - input) * (1.0f - input);
        } else {
            result = (float) (1.0f - Math.pow((1.0f - input), 2 * mFactor));
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxPullDistance = w / 8;
        mViewWidth = w;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                int distance = mLastX - x;
                mLastX = x;
                if ((distance > 0 && mManager.findLastCompletelyVisibleItemPosition() == mAdapter.getCount() - 1) ||
                        (distance < 0 && mManager.findFirstCompletelyVisibleItemPosition() == 0)) {
                    if (mRestoreX == 0) mRestoreX = x;
                    int max = distance > 0 ? mRestoreX : mViewWidth - mRestoreX;
                    int distanceX = (int) (mMaxPullDistance * getInterpolation(Math.abs(mRestoreX - x) / (float) max));
                    distanceX = distance > 0 ? distanceX : -distanceX;
                    mWonderItemsContainer.scrollTo(distanceX, 0);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mRestoreX > 0) {
                    View view = mManager.getChildAt(mManager.getChildCount() - 1);
                    if (view == null) view = mManager.getChildAt(0);
                    mAnimator.setIntValues(mWonderItemsContainer.getScrollX(), 0, mWonderItemsContainer.getScrollX() > 0 ? -view.getWidth() / 2 : view.getWidth() / 2);
                    mScrolledX = 0;
                    mAnimator.start();
                }

        }
        return mAnimator.isRunning() || super.dispatchTouchEvent(ev);
    }

    public void setListener(OnDayChangedListener listener) {
        mListener = listener;
    }

    private void initView() {
        mAnimator = new ValueAnimator();
        mAnimator.setDuration(200);
        mAnimator.addUpdateListener(this);
        mManager = new WheelLayoutManager(getContext());
        mManager.setOrientation(HORIZONTAL);
        mIndicatorList.setLayoutManager(mManager);
        mAdapter = new SuperAdapter<WheelItem>(getContext(), null, R.layout.wonder_indicaror_item) {
            @Override
            public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, WheelItem item) {
                holder.setEnabled(R.id.wonder_indicator_item_container, item.wonderful);
                holder.setEnabled(R.id.wonder_indicator_item, item.wonderful);
                holder.setSelected(R.id.wonder_indicator_item, item.selected);
                holder.setText(R.id.wonder_indicator_item, TimeUtils.getDayInMonth(item.time));
                holder.setTag(R.id.wonder_indicator_item, item);
                mTitle.setText(TimeUtils.getMonthInYear(item.time));
            }
        };
        mIndicatorList.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }

    private int mLastX;

    @Override
    public void onItemClick(View itemView, int viewType, int position) {
        WheelItem c = mAdapter.getItem(position);
        if (mLastPosition != position) {
            c.selected = true;
            itemView.findViewById(R.id.wonder_indicator_item).setSelected(true);
            mIndicatorList.smoothScrollToPosition(position);
            if (mLastPosition != -1) {
                mAdapter.getItem(mLastPosition).selected = false;
                mAdapter.notifyItemChanged(mLastPosition);
            }
            mLastPosition = position;
            if (mListener != null) mListener.onChanged(c.time);
        }
    }

    private int mScrolledX;

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int value = (int) animation.getAnimatedValue();
        int containerX = mWonderItemsContainer.getScrollX();
        if ((containerX > 0 && value <= 0) || (containerX < 0 && value >= 0)) {
            mWonderItemsContainer.scrollTo(0, 0);
            int scrollX = value - mScrolledX;
            mIndicatorList.scrollBy(scrollX, 0);
            mScrolledX = value;
        } else if ((containerX > 0 && value >= 0) || (containerX < 0 && value <= 0)) {
            mWonderItemsContainer.scrollTo(value, 0);
        } else {
            int scrollX = value - mScrolledX;
            mIndicatorList.scrollBy(scrollX, 0);
            mScrolledX = (int) animation.getAnimatedValue();
        }
        if (animation.getAnimatedFraction() >= 1) {
            mRestoreX = 0;
        }
    }

    public interface OnDayChangedListener {
        void onChanged(long time);
    }

    public static class WheelItem {
        public boolean wonderful = false;//这一天是否有数据
        public long time;//这一天的时间
        public boolean init = false;//是否已经查询过了
        public boolean selected = false;//是否被选中
    }

    public void init(List<WheelItem> items) {
        mAdapter.addAll(items);
    }

    public void notify(long time, boolean hasDate, boolean selected) {
        long startTime = TimeUtils.getSpecificDayStartTime(time);
        WheelItem item;
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            item = mAdapter.getItem(i);
            if (item.time / DAY_TIME == startTime / DAY_TIME) {
                item.wonderful = hasDate;
                item.selected = selected;
                if (mLastPosition != i & mLastPosition != -1) {
                    mAdapter.getItem(mLastPosition).selected = !selected;
                    mAdapter.notifyItemChanged(mLastPosition);
                }
                if (selected) mLastPosition = i;
                mAdapter.notifyItemChanged(i);
                return;
            }
        }
    }

    private void changeSelected(long time) {

    }

    public void scrollPositionToCenter() {
        mIndicatorList.smoothScrollToPosition(mLastPosition);
    }

    public boolean hasInit() {
        return mAdapter.getList() != null && mAdapter.getList().size() > 0;
    }

    private static class WheelLayoutManager extends LinearLayoutManager {

        public WheelLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            LinearSmoothScroller linearSmoothScroller = new SmoothToCenterScroller(recyclerView.getContext());
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }

        private class SmoothToCenterScroller extends LinearSmoothScroller {
            public static final int SNAP_TO_CENTER = 2;

            public SmoothToCenterScroller(Context context) {
                super(context);
            }

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return WheelLayoutManager.this
                        .computeScrollVectorForPosition(targetPosition);
            }

            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                if (snapPreference == SNAP_TO_CENTER) {
                    return (boxEnd - boxStart) / 2 - viewStart - (viewEnd - viewStart) / 2;
                }
                return super.calculateDtToFit(viewStart, viewEnd, boxStart, boxEnd, snapPreference);
            }


            @Override
            protected int calculateTimeForScrolling(int dx) {
                return Math.max(200, super.calculateTimeForScrolling(dx) * 3);
            }

            @Override
            protected int getHorizontalSnapPreference() {
                return SNAP_TO_CENTER;
            }
        }
    }
}
