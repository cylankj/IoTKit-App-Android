package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.support.galleryfinal.widget.zoonview.CircleImageView;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;


public class RelativesAndFriendsAdapter extends SuperAdapter<RelAndFriendBean> {



    public RelativesAndFriendsAdapter(Context context, List<RelAndFriendBean> items, IMulItemViewType<RelAndFriendBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, int layoutPosition, RelAndFriendBean item) {
        //如果没有备注名就显示别人昵称或者账号
        holder.setText(R.id.tv_username, (item.markName == null || item.markName.equals("")) ? item.alias : item.markName);
        holder.setText(R.id.tv_add_message, item.account);
        RoundedImageView userImag  = (RoundedImageView)holder.getView(R.id.iv_userhead);
        Glide.with(ContextUtils.getContext()).load(item.iconUrl)
                .error(R.drawable.img_me_list_head)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(userImag);
    }

    @Override
    protected IMulItemViewType<RelAndFriendBean> offerMultiItemViewType() {
        return new IMulItemViewType<RelAndFriendBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, RelAndFriendBean jfgFriendAccount) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_mine_relativesandfriends_list_items;
            }
        };
    }
}
