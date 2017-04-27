package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by hunt on 16-5-24.
 */

public class CamLandHistoryDateAdapter extends SuperAdapter<Long> {

    private int preIndex = -1;

    public void setCurrentFocusTime(long time) {
        preIndex = getIndexByTime(time);
        notifyDataSetChanged();
    }

    public CamLandHistoryDateAdapter(Context context, List<Long> items, int layoutResId) {
        super(context, items, layoutResId);
    }

    private int getIndexByTime(long timeFocus) {
        //得到凌晨时间戳
        long time = SystemClock.currentThreadTimeMillis();
        long timeStart = TimeUtils.startOfDay(timeFocus);
        //由于dateStartList是降序,所以需要Collections.reverseOrder()
        int index = Collections.binarySearch(getList(), timeStart, Collections.reverseOrder());
        if (index < 0) {
            index = -(index + 1);
            if (index < 0 || index > getList().size() - 1) {
                index = getList().size() - 1;
            }
        }
        Log.d("getIndexByTime", "getIndexByTime: " + index + " " + getList());
        Log.d("getIndexByTime", "getIndexByTime: performance: " + (SystemClock.currentThreadTimeMillis() - time));
        return index;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, Long item) {
        holder.setText(R.id.tv_item_content, TimeUtils.getSpecifiedDate(item));
        holder.setTextColor(R.id.tv_item_content, getContext().getResources().getColor(layoutPosition == preIndex ? R.color.color_4b9fd5 : R.color.color_white));
        holder.setTag(R.id.tv_item_content, item);
    }

}