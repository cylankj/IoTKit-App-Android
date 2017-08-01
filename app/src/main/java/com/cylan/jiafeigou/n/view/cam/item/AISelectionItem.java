package com.cylan.jiafeigou.n.view.cam.item;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mikepenz.fastadapter.items.AbstractItem;

/**
 * Created by yanzhendong on 2017/8/1.
 */

public class AISelectionItem extends AbstractItem<AISelectionItem, AISelectionItem.ViewHolder> {


    @Override
    public ViewHolder getViewHolder(View v) {
        return null;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
