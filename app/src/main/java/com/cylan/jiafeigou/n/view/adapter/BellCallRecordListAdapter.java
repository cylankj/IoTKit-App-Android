package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;
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

    /**
     * 正常模式，编辑模式
     */
    private int mode = 0;

    public BellCallRecordListAdapter(Context context, List<BellCallRecordBean> items,
                                     final int layoutId) {
        super(context, items, layoutId);
    }

    public synchronized void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    /**
     * @param lastVisiblePosition
     */
    public synchronized void reverseEdition(final boolean selected, final int lastVisiblePosition) {
        for (int i = 0; i < getCount(); i++) {
            BellCallRecordBean bean = getItem(i);
            if (bean.selected == selected) {
                bean.selected = !selected;
                if (i <= lastVisiblePosition)
                    notifyItemChanged(i);
            }
        }
    }

    /**
     * @param lastVisiblePosition
     */
    public synchronized void selectAll(final int lastVisiblePosition) {
        for (int i = 0; i < getCount(); i++) {
            BellCallRecordBean bean = getItem(i);
            if (bean.selected)
                continue;
            bean.selected = true;
            if (i <= lastVisiblePosition)
                notifyItemChanged(i);
        }
    }

    /**
     * @param lastVisiblePosition
     */
    public synchronized void selectNone(final int lastVisiblePosition) {
        for (int i = 0; i < getCount(); i++) {
            BellCallRecordBean bean = getItem(i);
            if (!bean.selected)
                continue;
            bean.selected = false;
            if (i <= lastVisiblePosition)
                notifyItemChanged(i);
        }
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType,
                       final int layoutPosition, BellCallRecordBean item) {
        holder.setText(R.id.tv_bell_list_item_date, item.date);
        holder.setText(R.id.tv_bell_list_item_time, item.timeStr);
        setAnswerState(item.answerState, (TextView) holder.getView(R.id.tv_bell_list_item_answer_state));
        holder.setTag(R.id.cv_bell_call_item, layoutPosition);
        if (simpleLongClickListener != null) {
            holder.setOnLongClickListener(R.id.cv_bell_call_item, simpleLongClickListener);
        }
        if (simpleLongClickListener != null) {
            holder.setOnClickListener(R.id.cv_bell_call_item, simpleClickListener);
        }
        setSelectState(holder, item);
    }

    private void setSelectState(SuperViewHolder holder, BellCallRecordBean item) {
        holder.setVisibility(R.id.imgv_bell_call_item_cover, item.selected ? View.VISIBLE : View.GONE);
    }

    private void setAnswerState(final int state, TextView textView) {
        textView.setText(state == 0 ? "未接听" : "已接听");
        ViewUtils.setDrawablePadding(textView, state == 0 ? R.drawable.icon_bell_dismiss : R.drawable.icon_bell_answer, 0);
    }

    private SimpleLongClickListener simpleLongClickListener;

    public void setSimpleLongClickListener(SimpleLongClickListener simpleLongClickListener) {
        this.simpleLongClickListener = simpleLongClickListener;
    }

    private SimpleClickListener simpleClickListener;

    public void setSimpleClickListener(SimpleClickListener simpleClickListener) {
        this.simpleClickListener = simpleClickListener;
    }

    public interface SimpleLongClickListener extends View.OnLongClickListener {
    }

    public interface SimpleClickListener extends View.OnClickListener {
    }
}
