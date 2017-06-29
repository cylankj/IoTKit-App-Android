package com.cylan.jiafeigou.n.view.adapter.item;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.photoselect.CircleImageView;
import com.cylan.jiafeigou.utils.JFGAccountURL;
import com.mikepenz.fastadapter.IClickable;
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

public class FriendGroupChildItem<Parent extends IItem & IExpandable & ISubItem & IClickable> extends AbstractExpandableItem<Parent, FriendGroupChildItem.ViewHolder, FriendGroupChildItem<Parent>> {
    public JFGFriendRequest friendRequest;
    public JFGFriendAccount friendAccount;
    public int childType;//0:request 1:friend

    public FriendGroupChildItem(JFGFriendAccount friendAccount) {
        this.friendAccount = friendAccount;
        this.friendRequest = null;
        this.childType = 1;
    }

    public FriendGroupChildItem(JFGFriendRequest friendRequest) {
        this.friendRequest = friendRequest;
        this.friendAccount = null;
        this.childType = 0;
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @SuppressLint("ResourceType")
    @Override
    public int getType() {
        return R.layout.fragment_mine_friends_items;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_mine_friends_items;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_accept_request)
        TextView accept;
        @BindView(R.id.tv_add_message)
        TextView message;
        @BindView(R.id.tv_username)
        TextView username;
        @BindView(R.id.iv_userhead)
        CircleImageView picture;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        String username;
        String message;
        String account;
        if (childType == 0) {
            username = TextUtils.isEmpty(friendRequest.alias) ? friendRequest.account : friendRequest.alias;
            message = friendRequest.sayHi;
            account = friendRequest.account;
        } else {
            username = TextUtils.isEmpty(friendAccount.alias) ? friendAccount.account : friendAccount.alias;
            message = friendAccount.account;
            account = friendAccount.account;
        }
        holder.accept.setVisibility(childType == 0 ? View.VISIBLE : View.GONE);
        holder.username.setText(username);
        holder.message.setText(message);
        Glide.with(holder.itemView.getContext())
                .load(new JFGAccountURL(account))
                .placeholder(R.drawable.img_me_list_head)
                .error(R.drawable.img_me_list_head)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.picture);
    }
}
