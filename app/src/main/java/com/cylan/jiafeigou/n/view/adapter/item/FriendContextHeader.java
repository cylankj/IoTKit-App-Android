package com.cylan.jiafeigou.n.view.adapter.item;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yanzhendong on 2017/6/29.
 */

public class FriendContextHeader extends AbstractItem<FriendContextHeader, FriendContextHeader.ViewHolder> {
    public String header;
    public List<FriendContextItem> children;
    public int headerType;

    public FriendContextHeader withHeader(String header) {
        this.header = header;
        return this;
    }

    public FriendContextHeader withChildren(List<FriendContextItem> children) {
        this.children = children;
        if (children != null) {
            for (FriendContextItem child : children) {
                child.withParent(this);
                withHeaderType(child.childType);
            }
        }
        return this;
    }

    public FriendContextHeader withHeaderType(int headerType) {
        this.headerType = headerType;
        return this;
    }


    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
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
        holder.listHeader.setText(header);
    }
}
