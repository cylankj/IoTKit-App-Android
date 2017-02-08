package com.cylan.jiafeigou.widget.wheel;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzd on 17-1-17.
 */

public class WonderIndicatorWheelView extends LinearLayout implements OnItemClickListener {

    private TextView mTitle;
    private RecyclerView mIndicatorList;
    private OnDayChangedListener mListener;
    private List<WheelItem> mWheelItems = new ArrayList<>(MAX_DAY_COUNT);
    private WheelItemQueryListener mItemQueryListener;
    private OnDayChangedListener mLoadMoreListener;
    private int mLastPosition = -1;
    private SuperAdapter<WheelItem> mAdapter;
    private WheelLayoutManager mManager;
    private boolean mShouldLoadMore = true;
    private boolean mHasLoadCompleted = true;

    private boolean hasInit = false;
    private static final int MAX_DAY_COUNT = 40;
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
        initView();
    }

    public void setListener(OnDayChangedListener listener) {
        mListener = listener;
    }

    public void setLoadMoreListener(OnDayChangedListener loadMoreListener) {
        mLoadMoreListener = loadMoreListener;
    }

    public void setItemQueryListener(WheelItemQueryListener itemQueryListener) {
        mItemQueryListener = itemQueryListener;
    }

    private void initView() {
        mManager = new WheelLayoutManager(getContext());
        mManager.setOrientation(HORIZONTAL);
        mIndicatorList.setLayoutManager(mManager);
        WheelItem item;
        for (int i = 0; i < MAX_DAY_COUNT; i++) {
            item = new WheelItem();
            mWheelItems.add(item);
        }
        mAdapter = new SuperAdapter<WheelItem>(getContext(), mWheelItems, R.layout.wonder_indicaror_item) {
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

    public boolean hasInit() {
        return hasInit;
    }

    public interface WheelItemQueryListener {
        void onQueryItem(long time);
    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {
        WheelItem c = mAdapter.getItem(position);
        if (mLastPosition != position) {
            mAdapter.getItem(mLastPosition).selected = false;
            c.selected = true;
            mManager.scrollPositionToCenter(position);
            mAdapter.notifyItemChanged(mLastPosition);
            mAdapter.notifyItemChanged(position);
            mLastPosition = position;
            if (mListener != null) mListener.onChanged(c.time);
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

    public void notify(long time, boolean wonderful) {
        long startTime = TimeUtils.getSpecificDayStartTime(time);
        int position = (int) ((startTime - mWheelItems.get(0).time) / DAY_TIME);
        mWheelItems.get(position).wonderful = wonderful;
    }

    public void init(long end) {
        long start = TimeUtils.getSpecificDayStartTime(end) - (MAX_DAY_COUNT - 5) * DAY_TIME;
        for (int i = 0; i < MAX_DAY_COUNT; i++) {
            mWheelItems.get(i).time = start + DAY_TIME * i;
        }
        mLastPosition = MAX_DAY_COUNT - 5;
        mWheelItems.get(mLastPosition).wonderful = true;
        mWheelItems.get(mLastPosition).selected = true;
        for (WheelItem item : mWheelItems) {
            mItemQueryListener.onQueryItem(item.time);
        }
        hasInit = true;
    }

    public void scrollPositionToCenter() {
        if (mManager != null) {
            mManager.scrollPositionToCenter(mLastPosition);
        }
    }

    private static class WheelLayoutManager extends LinearLayoutManager {

        private int mCenterPositionOffset = -1;

        public WheelLayoutManager(Context context) {
            super(context);
        }

        public void scrollPositionToCenter(int position) {
            if (mCenterPositionOffset == -1) {
                int width = getChildAt(0).getWidth();
                int width1 = getWidth();
                mCenterPositionOffset = (width1 - width) / 2;
            }
            scrollToPositionWithOffset(position, mCenterPositionOffset);
        }
    }
}
