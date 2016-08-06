package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * Created by cylan-hunt on 16-7-13.
 */
public class BellCallRecordListAdapter extends SuperAdapter<BellCallRecordBean> {


    public BellCallRecordListAdapter(Context context, List<BellCallRecordBean> items,
                                     final int layoutId) {
        super(context, items, layoutId);
    }


    @Override
    public void onBind(SuperViewHolder holder, int viewType,
                       int layoutPosition, BellCallRecordBean item) {
        holder.setText(R.id.tv_bell_list_item_date, item.date);
        holder.setText(R.id.tv_bell_list_item_time, item.timeStr);
        setAnswerState(item.answerState, (TextView) holder.getView(R.id.tv_bell_list_item_answer_state));
    }

    private void setAnswerState(final int state, TextView textView) {
        if (state == 0) {
            textView.setText("未接听");
            ViewUtils.setDrawablePadding(textView, R.drawable.icon_bell_dismiss, 0);
        }

    }
}
