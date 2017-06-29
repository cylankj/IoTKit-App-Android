package com.cylan.jiafeigou.n.view.adapter.item;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.mikepenz.fastadapter.IExpandable;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.ISubItem;
import com.mikepenz.fastadapter.commons.items.AbstractExpandableItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yanzhendong on 2017/6/29.
 */

public class FriendGroupParentItem<Parent extends IItem & IExpandable, SubItem extends IItem & ISubItem> extends AbstractExpandableItem<FriendGroupParentItem<Parent, SubItem>, FriendGroupParentItem.ViewHolder, SubItem> {
    public String title;

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    public FriendGroupParentItem<Parent, SubItem> withTitle(String title) {
        this.title = title;
        return this;
    }


    @SuppressLint("ResourceType")
    @Override
    public int getType() {
        return R.layout.layout_list_header;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.layout_list_header;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_friend_list_title)
        TextView listHeader;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        holder.listHeader.setText(title);
    }
}
