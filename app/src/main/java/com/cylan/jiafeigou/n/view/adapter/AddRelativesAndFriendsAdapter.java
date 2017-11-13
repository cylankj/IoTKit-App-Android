package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.FriendsReqBean;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.List;


public class AddRelativesAndFriendsAdapter extends SuperAdapter<FriendsReqBean> {

    private OnAcceptClickListener listener;

    public interface OnAcceptClickListener {
        void onAccept(SuperViewHolder holder, int viewType, int layoutPosition, FriendsReqBean item);
    }

    public void setOnAcceptClickListener(OnAcceptClickListener listener) {
        this.listener = listener;
    }

    public AddRelativesAndFriendsAdapter(Context context, List<FriendsReqBean> items, IMulItemViewType<FriendsReqBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, final int viewType, final int layoutPosition, final FriendsReqBean item) {
        holder.setText(R.id.tv_username, TextUtils.isEmpty(item.alias) ? item.account : item.alias);
        if (TextUtils.isEmpty(item.sayHi)) {
            holder.setText(R.id.tv_add_message, String.format(ContextUtils.getContext().getString(R.string.Tap3_FriendsAdd_RequestContents), TextUtils.isEmpty(item.alias) ? item.account : item.alias));
        } else {
            holder.setText(R.id.tv_add_message, item.sayHi);
        }
        //头像
        GlideApp.with(getContext())
                .load(item.iconUrl)
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into((ImageView) holder.getView(R.id.iv_userhead));
        if (layoutPosition == getItemCount() - 1) {
            holder.setVisibility(R.id.view_line, View.INVISIBLE);
        }

        holder.setOnClickListener(R.id.tv_accept_request, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAccept(holder, viewType, layoutPosition, item);
                }
            }
        });
    }

    @Override
    protected IMulItemViewType<FriendsReqBean> offerMultiItemViewType() {
        return new IMulItemViewType<FriendsReqBean>() {
            @Override

            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, FriendsReqBean jfgFriendRequest) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_mine_friends_items;
            }
        };
    }
}
