package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;


public class AddRelativesAndFriendsAdapter extends SuperAdapter<MineAddReqBean> {


    private OnAcceptClickLisenter lisenter;
    private RoundedImageView headImag;

    public interface OnAcceptClickLisenter {
        void onAccept(SuperViewHolder holder, int viewType, int layoutPosition, MineAddReqBean item);
    }

    public void setOnAcceptClickLisenter(OnAcceptClickLisenter lisenter) {
        this.lisenter = lisenter;
    }

    public AddRelativesAndFriendsAdapter(Context context, List<MineAddReqBean> items, IMulItemViewType<MineAddReqBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, final int viewType, final int layoutPosition, final MineAddReqBean item) {
        holder.setText(R.id.tv_username, item.alias);
        if(item.sayHi == null || "".equals(item.sayHi)){
            holder.setText(R.id.tv_add_message,item.alias+ContextUtils.getContext().getString(R.string.Tap3_FriendsAdd_RequestContents));
        }else {
            holder.setText(R.id.tv_add_message, item.sayHi);
        }

        headImag = holder.getView(R.id.iv_userhead);
        //头像
        Glide.with(getContext()).load(item.iconUrl)
                .asBitmap().centerCrop()
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new BitmapImageViewTarget(headImag) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        headImag.setImageDrawable(circularBitmapDrawable);
                    }
                });

        if (layoutPosition == getItemCount() - 1) {
            holder.setVisibility(R.id.view_line, View.INVISIBLE);
        }

        holder.setOnClickListener(R.id.tv_accept_request, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lisenter != null) {
                    lisenter.onAccept(holder, viewType, layoutPosition, item);
                }
            }
        });
    }

    @Override
    protected IMulItemViewType<MineAddReqBean> offerMultiItemViewType() {
        return new IMulItemViewType<MineAddReqBean>() {
            @Override

            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, MineAddReqBean jfgFriendRequest) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_mine_friends_request_add_items;
            }
        };
    }

}
