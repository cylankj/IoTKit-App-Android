package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.FriendsReqBean;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.lang.ref.WeakReference;
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
        MyViewTarget myViewTarget = new MyViewTarget(holder.getView(R.id.iv_userhead), getContext().getResources());
        Glide.with(getContext()).load(item.iconUrl)
                .asBitmap().centerCrop()
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(myViewTarget);
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

    private static class MyViewTarget extends BitmapImageViewTarget {
        private final WeakReference<Resources> resourcesWeakReference;
        private final WeakReference<RoundedImageView> imageViewWeakReference;

        public MyViewTarget(RoundedImageView view, Resources resources) {
            super(view);
            resourcesWeakReference = new WeakReference<Resources>(resources);
            imageViewWeakReference = new WeakReference<RoundedImageView>(view);
        }

        @Override
        protected void setResource(Bitmap resource) {
            super.setResource(resource);
            RoundedBitmapDrawable circularBitmapDrawable =
                    RoundedBitmapDrawableFactory.create(resourcesWeakReference.get(), resource);
            circularBitmapDrawable.setCircular(true);
            imageViewWeakReference.get().setImageDrawable(circularBitmapDrawable);
        }
    }

}
