package com.cylan.jiafeigou.n.view.cloud;

import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveCallInBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveCallOutBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveLeaveMesBean;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

/**
 * 作者：zsl
 * 创建时间：2016/10/8
 * 描述：
 */
public class LayoutHandler {

    private static final int LEAVE_MESG_TYPE = 0;
    private static final int VIDEO_CALL_OUT_TYPE = 2;
    private static final int VIDEO_CALL_IN_TYPE = 1;

    private ViewTypeMapCache viewTypeCache;
    private LayoutIdMapCache layoutIdMapCache;

    public void setViewTypeCache(ViewTypeMapCache viewTypeCache) {
        this.viewTypeCache = viewTypeCache;
    }

    public void setLayoutIdMapCache(LayoutIdMapCache layoutIdMapCache) {
        this.layoutIdMapCache = layoutIdMapCache;
    }

    public void handleLayout(SuperViewHolder holder, int viewType, int layoutPosition, CloudLiveBaseBean items) {
        Object o = items.data;
        LinearLayout.LayoutParams item = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (layoutPosition != 0) {
            item.setMargins(0, ViewUtils.dp2px(30), 0, 0);
            holder.itemView.setLayoutParams(item);
        } else {
            item.setMargins(0, ViewUtils.dp2px(11), 0, 0);
            holder.itemView.setLayoutParams(item);
        }

        switch (viewType) {
            case LEAVE_MESG_TYPE:
                CloudLiveLeaveMesBean cc = (CloudLiveLeaveMesBean) o;
                holder.setText(R.id.tv_voice_length, cc.getLeaveMesgLength());
                holder.setText(R.id.tv_time, cc.getLeveMesgTime());
                ImageView userHeadImag = holder.getView(R.id.iv_user_icon);
                //头像
                Glide.with(ContextUtils.getContext()).load(cc.userIcon)
                        .asBitmap()
                        .error(R.drawable.icon_mine_head_normal)
                        .placeholder(R.drawable.icon_mine_head_normal)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(new BitmapImageViewTarget(userHeadImag) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(ContextUtils.getContext().getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                userHeadImag.setImageDrawable(circularBitmapDrawable);
                            }
                        });

                if (cc.isRead()) {
                    holder.setVisibility(R.id.tv_is_read, View.VISIBLE);
                } else {
                    holder.setVisibility(R.id.tv_is_read, View.INVISIBLE);
                }
                break;

            case VIDEO_CALL_OUT_TYPE:
                CloudLiveCallOutBean callOutvideoBean = (CloudLiveCallOutBean) o;
                holder.setText(R.id.tv_time, callOutvideoBean.getVideoTime());
                ImageView userImag = holder.getView(R.id.iv_user_icon);
                //头像
                Glide.with(ContextUtils.getContext()).load(callOutvideoBean.userIcon)
                        .asBitmap()
                        .error(R.drawable.icon_mine_head_normal)
                        .placeholder(R.drawable.icon_mine_head_normal)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(new BitmapImageViewTarget(userImag) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(ContextUtils.getContext().getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                userImag.setImageDrawable(circularBitmapDrawable);
                            }
                        });

                if (callOutvideoBean.isHasConnet()) {
                    holder.setText(R.id.tv_voideo_talk_length, String.format(ContextUtils.getContext().getString(R.string.Tap1_iHome_CallDuration), callOutvideoBean.getVideoLength()));
                    holder.setImageDrawable(R.id.iv_call_out_icon, ContextUtils.getContext().getResources().getDrawable(R.drawable.album_icon_call));
                } else {
                    holder.setText(R.id.tv_voideo_talk_length, "未接通");
                    holder.setImageDrawable(R.id.iv_call_out_icon, ContextUtils.getContext().getResources().getDrawable(R.drawable.album_icon_missed_call));
                }
                break;

            case VIDEO_CALL_IN_TYPE:
                CloudLiveCallInBean callInvideoBean = (CloudLiveCallInBean) o;
                holder.setText(R.id.tv_call_in_time, callInvideoBean.getVideoTime());

                if (callInvideoBean.isHasConnet()) {
                    holder.setText(R.id.tv_call_in_talk_length, String.format(ContextUtils.getContext().getString(R.string.Tap1_iHome_CallDuration), callInvideoBean.getVideoLength()));
                    holder.setImageDrawable(R.id.iv_call_in_icon, ContextUtils.getContext().getResources().getDrawable(R.drawable.album_icon_incoming_call));
                } else {
                    holder.setText(R.id.tv_call_in_talk_length, ContextUtils.getContext().getString(R.string.EFAMILY_MISSED_CALL));
                    holder.setImageDrawable(R.id.iv_call_in_icon, ContextUtils.getContext().getResources().getDrawable(R.drawable.album_icon_missed_call));
                }

                break;
        }

    }
}
