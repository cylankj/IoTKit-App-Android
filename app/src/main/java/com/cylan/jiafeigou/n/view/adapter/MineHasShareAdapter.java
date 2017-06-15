package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.FriendBean;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/10/26
 * 描述：
 */
public class MineHasShareAdapter extends SuperAdapter<FriendBean> {

    private OnCancleShareListenter listenter;
    private RoundedImageView userImag;

    public interface OnCancleShareListenter {
        void onCancleShare(FriendBean item);
    }

    public void setOnCancleShareListenter(OnCancleShareListenter listenter) {
        this.listenter = listenter;
    }


    public MineHasShareAdapter(Context context, List<FriendBean> items, IMulItemViewType<FriendBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, final FriendBean item) {
        holder.setText(R.id.tv_username, TextUtils.isEmpty(item.markName) ? item.alias : item.markName);
        holder.setText(R.id.tv_friend_account, item.account);
        holder.setOnClickListener(R.id.tv_btn_cancle_share, v -> {
            if (listenter != null) {//存在复用的情况,不可取
                listenter.onCancleShare(item);
            }
        });
        userImag = holder.getView(R.id.iv_userhead);
        holder.itemView.setTag(item.iconUrl);
        //头像
        Glide.with(getContext()).load(item.iconUrl)
                .asBitmap().centerCrop()
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new BitmapImageViewTarget(userImag) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        if (item.iconUrl.equals(holder.itemView.getTag())) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            userImag.setImageDrawable(circularBitmapDrawable);
                        }
                    }
                });
    }

    @Override
    protected IMulItemViewType<FriendBean> offerMultiItemViewType() {
        return new IMulItemViewType<FriendBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, FriendBean account) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_has_share_to_friend_items;
            }
        };
    }
}
