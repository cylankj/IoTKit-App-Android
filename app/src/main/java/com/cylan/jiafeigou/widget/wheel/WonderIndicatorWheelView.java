package com.cylan.jiafeigou.widget.wheel;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;


/**
 * Created by yzd on 17-1-17.
 */

public class WonderIndicatorWheelView extends LinearLayout implements OnItemClickListener, ValueAnimator.AnimatorUpdateListener {

    private TextView mTitle;
    private RecyclerView mIndicatorList;
    private OnDayChangedListener mListener;
    private int mLastPosition = -1;
    private SuperAdapter<WheelItem> mAdapter;
    //    private WheelLayoutManager mManager;
    private static final long DAY_TIME = 24 * 60 * 60 * 1000L;

    private final int HALF_SCREEN_COUNT;
    private final int ITEM_WIDTH;
    private final float HALF_SCREEN_WIDTH;

    public WonderIndicatorWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.wonder_time_indicator, this, true);
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        ITEM_WIDTH = getResources().getDimensionPixelSize(R.dimen.x27);//item是固定宽度的。
        //原理，需要保证最左边一个有数据的一天的左边还有N个item.所以最左边的一个item才能移到中间。
        //item之间没有间隙。
        HALF_SCREEN_COUNT = screenWidth / 2 / ITEM_WIDTH + 1;//半屏有这么多个
        HALF_SCREEN_WIDTH = screenWidth / 2;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle = (TextView) findViewById(R.id.wonder_item_title);
        mIndicatorList = (RecyclerView) findViewById(R.id.wonder_item_indicator_list);
        initView();
    }

    public void setListener(OnDayChangedListener listener) {
        mListener = listener;
    }

    private void initView() {
        WheelLayoutManager mManager = new WheelLayoutManager(getContext());
        mManager.setOrientation(HORIZONTAL);
        mIndicatorList.setLayoutManager(mManager);
        mAdapter = new SuperAdapter<WheelItem>(getContext(), null, R.layout.wonder_indicaror_item) {
            @Override
            public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, WheelItem item) {
                holder.setEnabled(R.id.wonder_indicator_item, item.wonderful);
                holder.setSelected(R.id.wonder_indicator_item, item.selected);
                holder.setText(R.id.wonder_indicator_item, TimeUtils.getDayInMonth(item.time));
                holder.setTag(R.id.wonder_indicator_item, item);
                mTitle.setText(TimeUtils.getMonthInYear(item.time));
                Log.d("onBind", "onBind: " + layoutPosition);
            }
        };
        mIndicatorList.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }

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

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
    }

    public interface OnDayChangedListener {
        void onChanged(long time);
    }

    public static class WheelItem implements Comparable<WheelItem> {
        public boolean wonderful = false;//这一天是否有数据
        public long time;//这一天的时间
        public boolean init = false;//是否已经查询过了
        public boolean selected = false;//是否被选中

        @Override
        public String toString() {
            return "WheelItem{" +
                    "wonderful=" + wonderful +
                    ", time=" + time +
                    ", init=" + init +
                    ", selected=" + selected +
                    '}';
        }

        @Override
        public int compareTo(@NonNull WheelItem another) {
            return (int) (time - another.time);
        }
    }

    public void init(List<WheelItem> items) {
        if (ListUtils.getSize(items) == 0) return;
        Collections.reverse(items);//反向一下。
        //需要auto append data,左边右边都需要填充数据
        AppLogger.d(String.format(Locale.getDefault(), "initSize half screen item counts:%s", HALF_SCREEN_COUNT));
        long startTime = TimeUtils.getSpecificDayStartTime(items.get(0).time);
        //头也加
        for (int j = 1; j < HALF_SCREEN_COUNT + 1; j++) {
            WheelItem item = new WheelItem();
            item.time = startTime - j * 24 * 3600 * 1000L;
            items.add(0, item);
        }
        //尾部也加
        startTime = items.get(items.size() - 1).time;
        for (int j = 1; j < HALF_SCREEN_COUNT + 1; j++) {
            WheelItem item = new WheelItem();
            item.time = startTime + j * 24 * 3600 * 1000L;
            items.add(item);
        }
        mAdapter.clear();
        mAdapter.addAll(items);
        post(() -> {
            final int rightIndex = findIndexFromRight();
            AppLogger.d("rightIndex: " + rightIndex);
            //自动偏移
            float scrollX = mIndicatorList.computeHorizontalScrollOffset();
            int targetOffset = ITEM_WIDTH * rightIndex;
            AppLogger.d("scrollX:" + scrollX + ",targetOffset:" + targetOffset);
            mIndicatorList.smoothScrollBy((int) (targetOffset - scrollX - HALF_SCREEN_WIDTH + ITEM_WIDTH / 2), 0);
        });
    }

    private int findIndexFromRight() {
        int finalSize = ListUtils.getSize(mAdapter.getList());
        for (int i = finalSize - 1; i >= 0; i--) {
            if (mAdapter.getItem(i).wonderful) {
                return i;
            }
        }
        return -1;
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
