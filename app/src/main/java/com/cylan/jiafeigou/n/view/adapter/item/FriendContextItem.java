package com.cylan.jiafeigou.n.view.adapter.item;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.JFGAccountURL;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by yanzhendong on 2017/6/29.
 */
public class FriendContextItem extends AbstractItem<FriendContextItem, FriendContextItem.ViewHolder> implements Parcelable {
    public FriendContextHeader parent;
    public JFGFriendRequest friendRequest;
    public JFGFriendAccount friendAccount;
    public int childType;//0:request 1:friend

    public FriendContextItem(JFGFriendAccount friendAccount) {
        this.friendAccount = friendAccount;
        this.friendRequest = null;
        this.childType = 1;
    }

    public FriendContextItem(JFGFriendRequest friendRequest) {
        this.friendRequest = friendRequest;
        this.friendAccount = null;
        this.childType = 0;
    }

    public FriendContextItem withParent(FriendContextHeader parent) {
        this.parent = parent;
        return this;
    }

    public String getAlias() {
        if (childType == 1) {
            return TextUtils.isEmpty(friendAccount.markName) ? TextUtils.isEmpty(friendAccount.alias) ? friendAccount.account : friendAccount.alias : friendAccount.markName;
        } else {
            return TextUtils.isEmpty(friendRequest.alias) ? friendRequest.account : friendRequest.alias;
        }
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
        public TextView accept;
        @BindView(R.id.tv_add_message)
        public TextView message;
        public @BindView(R.id.tv_username)
        TextView username;
        @BindView(R.id.iv_userhead)
        public CircleImageView picture;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        String username = getAlias();
        String message;
        String account;
        if (childType == 0) {
            message = friendRequest.sayHi;
            account = friendRequest.account;
        } else {
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
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.picture);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mIdentifier);
        dest.writeSerializable(this.friendRequest);
        dest.writeSerializable(this.friendAccount);
        dest.writeInt(this.childType);
    }

    protected FriendContextItem(Parcel in) {
        this.mIdentifier = in.readLong();
        this.friendRequest = (JFGFriendRequest) in.readSerializable();
        this.friendAccount = (JFGFriendAccount) in.readSerializable();
        this.childType = in.readInt();
    }

    public static final Creator<FriendContextItem> CREATOR = new Creator<FriendContextItem>() {
        @Override
        public FriendContextItem createFromParcel(Parcel source) {
            return new FriendContextItem(source);
        }

        @Override
        public FriendContextItem[] newArray(int size) {
            return new FriendContextItem[size];
        }
    };
}
