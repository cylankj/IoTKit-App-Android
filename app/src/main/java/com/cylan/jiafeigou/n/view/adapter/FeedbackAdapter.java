package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.FeedbackManager;
import com.cylan.jiafeigou.base.module.IManager;
import com.cylan.jiafeigou.cache.db.module.FeedBackBean;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.cylan.jiafeigou.base.module.FeedbackManager.TASK_STATE_FAILED;
import static com.cylan.jiafeigou.base.module.FeedbackManager.TASK_STATE_IDLE;
import static com.cylan.jiafeigou.base.module.FeedbackManager.TASK_STATE_STARTED;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/18 15:43
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class FeedbackAdapter extends SuperAdapter<FeedBackBean> {

    private static final int TYPE_COUNT = 2;

    private static final int TYPE_SERVER = 0;//服务端类型

    private static final int TYPE_Client = 1;//客户端类型

    private static FeedbackManager.SubmitFeedbackTask submitTask;
    private IManager<FeedBackBean, FeedbackManager.SubmitFeedbackTask> manager;
    private OnResendFeedBackListener resendFeedBack;
    private String portraitUrl;

    public interface OnResendFeedBackListener {
        void onResend(SuperViewHolder holder, FeedBackBean item, int position);
    }

    public void setOnResendFeedBack(OnResendFeedBackListener resendFeedBack) {
        this.resendFeedBack = resendFeedBack;
    }

    public FeedbackAdapter(Context context,
                           List<FeedBackBean> items,
                           IMulItemViewType<FeedBackBean> mulItemViewType) {
        super(context, items, mulItemViewType);
        manager = FeedbackManager.getInstance();
        JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        portraitUrl = account == null ? "" : account.getPhotoUrl();
    }

    private void refreshUrl() {
        JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        portraitUrl = account == null ? "" : account.getPhotoUrl();
    }

    private int itemLoading(long time) {
        FeedbackManager.SubmitFeedbackTask task = manager.getTask(time);
        if (task != null && task.getBackBean() != null) {
            if (task.getBackBean().getMsgTime() == time) {
                return task.getTaskState();
            }
        }
        return TASK_STATE_IDLE;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, FeedBackBean item) {
        if (viewType == 1) {     //客户端
            holder.setText(R.id.tv_mine_suggestion_client_time, getNowDate(item.getMsgTime()));
            holder.setText(R.id.tv_mine_suggestion_client_speak, item.getContent());

            ImageView iv_send_pro = holder.getView(R.id.iv_send_pro);
            ProgressBar send_pro = holder.getView(R.id.send_pro);
            int state = itemLoading(item.getMsgTime());
            if (state == TASK_STATE_STARTED) {
                //显示正在发送
                iv_send_pro.setVisibility(View.INVISIBLE);
                send_pro.setVisibility(View.VISIBLE);
            } else if (state == TASK_STATE_FAILED) {
                //显示发送失败
                iv_send_pro.setVisibility(View.VISIBLE);
                holder.setImageDrawable(R.id.iv_send_pro, getContext().getResources().getDrawable(R.drawable.album_icon_caution));
            } else {
                //显示发送成功
                holder.setVisibility(R.id.iv_send_pro, View.GONE);
                send_pro.setVisibility(View.GONE);
            }

            holder.setOnClickListener(R.id.iv_send_pro, v -> {
                if (resendFeedBack != null) {
                    resendFeedBack.onResend(holder, item, layoutPosition);
                }
            });

            ImageView clientImage = holder.getView(R.id.iv_mine_suggestion_client);
            MyImageViewTarget myImageViewTarget = new MyImageViewTarget(clientImage, getContext().getResources());
            if (TextUtils.isEmpty(portraitUrl))
                refreshUrl();
            Glide.with(getContext()).load(portraitUrl)
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
            if (item.getContent().length() <= 13) {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                textView.setLayoutParams(lp);
            } else {
                lp.width = ViewUtils.dp2px(230);
                textView.setLayoutParams(lp);
            }

//            if (item.isShowTime) {
//                holder.setVisibility(R.id.tv_mine_suggestion_server_time, View.VISIBLE);
//            } else {
            holder.setVisibility(R.id.tv_mine_suggestion_server_time, View.INVISIBLE);
//            }

            holder.setText(R.id.tv_mine_suggestion_server_speak, item.getContent());

            holder.setText(R.id.tv_mine_suggestion_server_time, getNowDate(item.getMsgTime()));

            holder.setBackgroundResource(R.id.iv_mine_suggestion_server, R.drawable.pic_head);
        }
    }

    @Override
    protected IMulItemViewType<FeedBackBean> offerMultiItemViewType() {
        return new IMulItemViewType<FeedBackBean>() {
            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position, FeedBackBean bean) {
                return bean.getViewType(); //0.显示服务端 ，1.显示客户端
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
     * @param time
     */
    public String getNowDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return sdf.format(new Date(time));
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
