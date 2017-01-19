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
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.superadapter.OnItemClickListener;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzd on 17-1-17.
 */

public class WonderIndicatorWheelView extends LinearLayout implements OnItemClickListener {

    private TextView mTitle;
    private RecyclerView mIndicatorList;
    private OnDayChangedListener mListener;
    private List<Item> mCacheItems = new ArrayList<>();
    private OnDayChangedListener mLoadMoreListener;
    private long mLastQueryTime = Long.MAX_VALUE;
    private Item mLastSelectedItem;
    private SuperAdapter<Item> mAdapter;
    private LinearLayoutManager mManager;
    private boolean mShouldLoadMore = true;

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

    private void initView() {
        mManager = new LinearLayoutManager(getContext());
        mManager.setOrientation(HORIZONTAL);
        mIndicatorList.setLayoutManager(mManager);

        mAdapter = new SuperAdapter<Item>(getContext(), null, R.layout.wonder_indicaror_item) {
            @Override
            public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, Item item) {
                holder.setEnabled(R.id.wonder_indicator_item, item.hasData);
                holder.setSelected(R.id.wonder_indicator_item, item.isSelected);
                holder.setText(R.id.wonder_indicator_item, TimeUtils.getDayInMonth(item.startTime));
                holder.setTag(R.id.wonder_indicator_item, item);
                mTitle.setText(TimeUtils.getMonthInYear(item.startTime));
            }
        };
        mAdapter.setOnItemClickListener(this);
        mIndicatorList.setAdapter(mAdapter);
        mIndicatorList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && mShouldLoadMore) {
                    boolean first = !recyclerView.canScrollHorizontally(-1);
                    if (first && mLoadMoreListener != null) {
                        long time = mAdapter.getItem(0).startTime - 12 * 60 * 60 * 1000L;
                        mLoadMoreListener.onChanged(time);
                    }
                }
            }
        });
    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {
        Item c = mAdapter.getItem(position);
        if (mLastSelectedItem != c) {
            if (mLastSelectedItem != null) {
                mLastSelectedItem.isSelected = false;
                int i = mAdapter.getList().indexOf(mLastSelectedItem);
                mAdapter.notifyItemChanged(i);
            }
            c.isSelected = true;
            mAdapter.notifyItemChanged(position);
            if (mListener != null) mListener.onChanged(c.endTime);
        }
        mLastSelectedItem = c;
    }

    public interface OnDayChangedListener {
        void onChanged(long time);
    }

    public static class Item {
        public boolean hasData = false;
        public long startTime;
        public long endTime;
        public boolean isSelected;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Item)) return false;
            return TimeUtils.getSpecificDayStartTime(startTime) == TimeUtils.getSpecificDayStartTime(((Item) o).startTime);
        }
    }

    public void setTimeLineItems(List<Item> items) {
        List<Item> filter = filter(items);
        if (filter.size() == 0) mShouldLoadMore = false;
        mAdapter.addAll(0, filter);
        if (mAdapter.getCount() > 35) mShouldLoadMore = false;//最多一个月
        if (mLastSelectedItem == null) {
            mLastSelectedItem = mAdapter.getItem(mAdapter.getCount() - 1);
            mLastSelectedItem.isSelected = true;
        }
    }


    //用于第一次的初始化
    public void addTimeLineItem(Item item) {
        if (mAdapter.getCount() == 0) {
            item.isSelected = true;
            mAdapter.add(item);
            mIndicatorList.scrollToPosition(mAdapter.getCount() - 1);
        }
    }

    private List<Item> filter(List<Item> items) {
        Item item = mAdapter.getItem(0);
        long time = item == null ? Long.MAX_VALUE : TimeUtils.getSpecificDayStartTime(item.startTime);
        List<Item> result = new ArrayList<>();
        for (Item item1 : items) {
            if (item1.endTime < time) result.add(item1);
        }
        return result;
    }
}
