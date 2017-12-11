package com.cylan.jiafeigou.n.view.cam.item;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;


public class LoadMoreItem extends AbstractItem<LoadMoreItem, LoadMoreItem.ViewHolder> {

    @SuppressLint("ResourceType")
    @Override
    public int getType() {
        return R.layout.layout_item_cam_load_more;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.layout_item_cam_load_more;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
//        if (isEnabled()) {
//            holder.itemView.setBackgroundResource(FastAdapterUIUtils.getSelectableBackground(holder.itemView.getContext()));
//        }
    }

    @Override
    public void unbindView(ViewHolder holder) {

    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

//        protected ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
//            progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        }
    }
}

