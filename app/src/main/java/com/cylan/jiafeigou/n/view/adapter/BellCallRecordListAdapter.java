package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cylan-hunt on 16-7-13.
 */
public class BellCallRecordListAdapter extends SuperAdapter<BellCallRecordBean> {

    private final Object object = new Object();
    /**
     * 正常模式，编辑模式
     */
    private int mode = 0;

    private int itemWidth;
    private int itemHeight;

    private List<BellCallRecordBean> mRemovedList = new ArrayList<>(32);

    public BellCallRecordListAdapter(Context context, List<BellCallRecordBean> items,
                                     final int layoutId, LoadImageListener loadImageListener) {
        super(context, items, layoutId);
        this.loadImageListener = loadImageListener;
    }

    private LoadImageListener loadImageListener;


    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void setItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    /**
     * @param lastVisiblePosition
     */
    public void reverseEdition(final boolean selected, final int lastVisiblePosition) {
        synchronized (object) {
            for (int i = 0; i < getCount(); i++) {
                BellCallRecordBean bean = getItem(i);
                if (bean.selected == selected) {
                    bean.selected = !selected;
                    if (i <= lastVisiblePosition)
                        notifyItemChanged(i);
                }
            }
        }
    }

    /**
     * @param lastVisiblePosition
     */
    public void selectAll(final int lastVisiblePosition) {
        synchronized (object) {
            for (int i = 0; i < getCount(); i++) {
                BellCallRecordBean bean = getItem(i);
                if (bean.selected)
                    continue;
                bean.selected = true;
                if (i <= lastVisiblePosition)
                    notifyItemChanged(i);
            }
        }
    }

    /**
     * @param lastVisiblePosition
     */
    public void selectNone(final int lastVisiblePosition) {
        synchronized (object) {
            for (int i = 0; i < getCount(); i++) {
                BellCallRecordBean bean = getItem(i);
                if (!bean.selected)
                    continue;
                bean.selected = false;
                if (i <= lastVisiblePosition)
                    notifyItemChanged(i);
            }
        }
    }

    public void remove() {
        synchronized (object) {
            mRemovedList.clear();
            for (int i = getCount() - 1; i >= 0; i--) {
                BellCallRecordBean bean = getItem(i);
                if (!bean.selected)
                    continue;
                mRemovedList.add(bean);
                remove(i);

            }
        }
    }

    public void reverseItemSelectedState(final int position) {
        synchronized (object) {
            BellCallRecordBean bean = getItem(position);
            if (bean == null) {
                AppLogger.d("bean is null");
                return;
            }
            bean.selected = !bean.selected;
            notifyItemChanged(position);
        }
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType,
                       final int layoutPosition, BellCallRecordBean item) {
        holder.setText(R.id.tv_bell_list_item_date, item.date);
        holder.setText(R.id.tv_bell_list_item_time, item.timeStr);
        setAnswerState(item.answerState, holder.getView(R.id.tv_bell_list_item_answer_state));
        if (simpleLongClickListener != null) {
            holder.setOnLongClickListener(R.id.cv_bell_call_item, simpleLongClickListener);
        }
        if (simpleLongClickListener != null) {
            holder.setOnClickListener(R.id.cv_bell_call_item, simpleClickListener);
        }
        setSelectState(holder, item);
        if (loadImageListener != null)
            loadImageListener.loadMedia(item, holder.getView(R.id.imgv_bell_call_item_thumbnail));
    }

    private void setSelectState(SuperViewHolder holder, BellCallRecordBean item) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) holder.getView(R.id.imgv_bell_call_item_cover).getLayoutParams();
        lp.width = itemWidth;
        lp.height = itemHeight;
        holder.setVisibility(R.id.imgv_bell_call_item_cover, item.selected ? View.VISIBLE : View.GONE);
    }

    private void setAnswerState(final int state, TextView textView) {
        textView.setText(state == 0 ? mContext.getString(R.string.CALL_NOT_ANSWER) : mContext.getString(R.string.CALL_ANSWERD));
        ViewUtils.setDrawablePadding(textView, state == 0 ? R.drawable.doorbell_icon_not_connected : R.drawable.doorbell_icon_connect, 0);
    }

    private SimpleLongClickListener simpleLongClickListener;

    public void setSimpleLongClickListener(SimpleLongClickListener simpleLongClickListener) {
        this.simpleLongClickListener = simpleLongClickListener;
    }

    private SimpleClickListener simpleClickListener;

    public void setSimpleClickListener(SimpleClickListener simpleClickListener) {
        this.simpleClickListener = simpleClickListener;
    }

    public List<BellCallRecordBean> getSelectedList() {
        mRemovedList.clear();
        for (int i = getCount() - 1; i >= 0; i--) {
            BellCallRecordBean bean = getItem(i);
            if (bean.selected) {
                mRemovedList.add(bean);
            }
            remove(i);

        }
        return mRemovedList;
    }

    public interface SimpleLongClickListener extends View.OnLongClickListener {
    }

    public interface SimpleClickListener extends View.OnClickListener {
    }

    public interface LoadImageListener {
        void loadMedia(BellCallRecordBean item, ImageView imageView);
    }
}
