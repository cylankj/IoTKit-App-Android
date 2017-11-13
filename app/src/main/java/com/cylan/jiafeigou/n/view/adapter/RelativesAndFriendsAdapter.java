package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.FriendBean;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.util.List;


public class RelativesAndFriendsAdapter extends SuperAdapter<FriendBean> {

    public RelativesAndFriendsAdapter(Context context, List<FriendBean> items, IMulItemViewType<FriendBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, int layoutPosition, FriendBean item) {
        //如果没有备注名就显示别人昵称或者账号
        holder.setText(R.id.tv_username, (item.markName == null || item.markName.equals("")) ? item.alias : item.markName);
        holder.setText(R.id.tv_add_message, item.account);
        RoundedImageView userImag = holder.getView(R.id.iv_userhead);
        //头像
        GlideApp.with(getContext()).load(item.iconUrl)
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(userImag);
    }

    @Override
    protected IMulItemViewType<FriendBean> offerMultiItemViewType() {
        return new IMulItemViewType<FriendBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, FriendBean jfgFriendAccount) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_mine_friends_list_items;
            }
        };
    }
}
