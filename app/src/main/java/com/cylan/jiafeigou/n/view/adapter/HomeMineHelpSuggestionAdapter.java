package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.MineHelpSuggestionBean;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/18 15:43
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMineHelpSuggestionAdapter extends SuperAdapter<MineHelpSuggestionBean> {

    private static final int TYPE_COUNT = 2;

    private static final int TYPE_SERVER = 0;//服务端类型

    private static final int TYPE_Client = 1;//客户端类型

    private OnResendFeedBackListener resendFeedBack;
    private AnimationDrawable animationDrawable;

    public interface OnResendFeedBackListener {
        void onResend(SuperViewHolder holder, MineHelpSuggestionBean item, int position);
    }

    public void setOnResendFeedBack(OnResendFeedBackListener resendFeedBack) {
        this.resendFeedBack = resendFeedBack;
    }

    public HomeMineHelpSuggestionAdapter(Context context,
                                         List<MineHelpSuggestionBean> items,
                                         IMulItemViewType<MineHelpSuggestionBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, MineHelpSuggestionBean item) {
        if (viewType == 1) {     //客户端
            TextView textView = holder.getView(R.id.tv_mine_suggestion_client_speak);
            ViewGroup.LayoutParams lp = textView.getLayoutParams();
            // 动态改变条目的长度
            if (item.getText().length() <= 13) {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                textView.setLayoutParams(lp);
            } else {
                lp.width = ViewUtils.dp2px(230);
                textView.setLayoutParams(lp);
            }

            holder.setText(R.id.tv_mine_suggestion_client_time, getNowDate(item.getDate()));
            holder.setText(R.id.tv_mine_suggestion_client_speak, item.getText());

            if (item.isShowTime) {
                holder.setVisibility(R.id.tv_mine_suggestion_client_time, View.VISIBLE);
            } else {
                holder.setVisibility(R.id.tv_mine_suggestion_client_time, View.INVISIBLE);
            }

            ImageView iv_send_pro = holder.getView(R.id.iv_send_pro);

            if (item.pro_falag == 0) {
                //显示正在发送
                iv_send_pro.setVisibility(View.VISIBLE);
                iv_send_pro.setImageDrawable(null);
                iv_send_pro.setBackgroundResource(R.drawable.feekback_loading);
                animationDrawable = (AnimationDrawable) iv_send_pro.getBackground();
                animationDrawable.start();

            } else if (item.pro_falag == 1) {
                //显示发送失败
                holder.setImageDrawable(R.id.iv_send_pro, getContext().getResources().getDrawable(R.drawable.album_icon_caution));
            } else {
                //显示发送成功
                holder.setVisibility(R.id.iv_send_pro, View.GONE);
                if(animationDrawable != null){
                    animationDrawable.stop();
                    animationDrawable = null;
                }
                iv_send_pro.clearAnimation();
            }

            holder.setOnClickListener(R.id.iv_send_pro, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (resendFeedBack != null) {
                        resendFeedBack.onResend(holder, item, layoutPosition);
                    }
                }
            });

            ImageView clientImage = holder.getView(R.id.iv_mine_suggestion_client);
            MyImageViewTarget myImageViewTarget = new MyImageViewTarget(clientImage, getContext().getResources());
            Glide.with(getContext()).load(item.getIcon())
                    .asBitmap()
                    .error(R.drawable.icon_mine_head_normal)
                    .centerCrop()
                    .placeholder(R.drawable.icon_mine_head_normal)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(myImageViewTarget);

        } else {     //服务端
            TextView textView = holder.getView(R.id.tv_mine_suggestion_server_speak);
            ViewGroup.LayoutParams lp = textView.getLayoutParams();
            // 动态改变条目的长度
            if (item.getText().length() <= 13) {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                textView.setLayoutParams(lp);
            } else {
                lp.width = ViewUtils.dp2px(230);
                textView.setLayoutParams(lp);
            }

            if (item.isShowTime) {
                holder.setVisibility(R.id.tv_mine_suggestion_server_time, View.VISIBLE);
            } else {
                holder.setVisibility(R.id.tv_mine_suggestion_server_time, View.INVISIBLE);
            }

            holder.setText(R.id.tv_mine_suggestion_server_speak, item.getText());

            holder.setText(R.id.tv_mine_suggestion_server_time, getNowDate(item.getDate()));

            holder.setBackgroundResource(R.id.iv_mine_suggestion_server, R.drawable.pic_head);
        }
    }

    @Override
    protected IMulItemViewType<MineHelpSuggestionBean> offerMultiItemViewType() {
        return new IMulItemViewType<MineHelpSuggestionBean>() {
            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position, MineHelpSuggestionBean bean) {
                return bean.type; //0.显示服务端 ，1.显示客户端
            }

            @Override
            public int getLayoutId(int viewType) {
                return viewType == TYPE_SERVER ?
                        R.layout.fragment_mine_suggestion_server :
                        R.layout.fragment_mine_suggestion_client;
            }
        };
    }

    /**
     * 获得当前日期的方法
     *
     * @param magDate
     */
    public String getNowDate(String magDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String nowDate = sdf.format(new Date(Long.parseLong(magDate)));
        return nowDate;
    }

    private static class MyImageViewTarget extends BitmapImageViewTarget {

        private final WeakReference<Resources> resourcesWeakReference;
        private final WeakReference<ImageView> imageViewWeakReference;

        public MyImageViewTarget(ImageView view, Resources resources) {
            super(view);
            resourcesWeakReference = new WeakReference<Resources>(resources);
            imageViewWeakReference = new WeakReference<ImageView>(view);
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
