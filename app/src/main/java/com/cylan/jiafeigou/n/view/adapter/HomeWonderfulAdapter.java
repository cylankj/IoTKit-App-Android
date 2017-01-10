package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.WonderGlideURL;
import com.cylan.jiafeigou.utils.WonderGlideVideoThumbURL;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * Created by chen on 6/6/16.
 */


public class HomeWonderfulAdapter extends SuperAdapter<MediaBean> {


    private WonderfulItemClickListener deviceItemClickListener;
    private WonderfulItemLongClickListener deviceItemLongClickListener;

    private LoadMediaListener loadMediaListener;

    public HomeWonderfulAdapter(Context context, List<MediaBean> items,
                                IMulItemViewType<MediaBean> mulItemViewType) {
        super(context, items, mulItemViewType);

    }

    public void setWonderfulItemClickListener(WonderfulItemClickListener deviceItemClickListener) {
        this.deviceItemClickListener = deviceItemClickListener;
    }

    public void setWonderfulItemLongClickListener(WonderfulItemLongClickListener deviceItemLongClickListener) {
        this.deviceItemLongClickListener = deviceItemLongClickListener;
    }

    public void setLoadMediaListener(LoadMediaListener loadMediaListener) {
        this.loadMediaListener = loadMediaListener;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, MediaBean item) {
        if (viewType != MediaBean.TYPE_LOAD) {
            initClickListener(holder, layoutPosition);
            handleState(holder, item);
        }
    }

    private void initClickListener(SuperViewHolder holder, final int layoutPosition) {
        ViewCompat.setTransitionName(holder.getView(R.id.iv_wonderful_item_content),
                String.valueOf(layoutPosition) + JConstant.KEY_SHARED_ELEMENT_TRANSITION_NAME_SUFFIX);
        holder.setOnClickListener(R.id.iv_wonderful_item_content, deviceItemClickListener);
        holder.setOnClickListener(R.id.tv_wonderful_item_share, deviceItemClickListener);
        holder.setOnClickListener(R.id.tv_wonderful_item_delete, deviceItemClickListener);
        holder.setOnLongClickListener(R.id.rLayout_wonderful_item_wonder, deviceItemLongClickListener);
    }

    private void handleState(SuperViewHolder holder, MediaBean bean) {
        //时间
        holder.setText(R.id.tv_wonderful_item_date, TimeUtils.getHH_MM(bean.time * 1000));
        //来自摄像头
        if (TextUtils.isEmpty(bean.place)) {
            holder.setVisibility(R.id.tv_wonderful_item_device_name, View.GONE);
        } else {
            holder.setText(R.id.tv_wonderful_item_device_name, bean.place);
            holder.setVisibility(R.id.tv_wonderful_item_device_name, View.VISIBLE);
        }
        if (bean.msgType == MediaBean.TYPE_PIC) {
            Glide.with(getContext()).load(new WonderGlideURL(bean))
                    .placeholder(R.drawable.wonderful_pic_place_holder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) holder.getView(R.id.iv_wonderful_item_content));
        } else if (bean.msgType == MediaBean.TYPE_VIDEO) {
            Glide.with(getContext()).load(new WonderGlideVideoThumbURL(bean))
                    .placeholder(R.drawable.wonderful_pic_place_holder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) holder.getView(R.id.iv_wonderful_item_content));
        }
    }

    @Override
    protected IMulItemViewType<MediaBean> offerMultiItemViewType() {
        return new IMulItemViewType<MediaBean>() {
            @Override
            public int getViewTypeCount() {
                return 3;
            }

            @Override
            public int getItemViewType(int position, MediaBean mediaBean) {
                return mediaBean.msgType;//0:image view  1:videoView
            }

            @Override
            public int getLayoutId(int viewType) {
                return viewType == MediaBean.TYPE_PIC ?
                        R.layout.layout_item_picture_wonderful : (viewType == MediaBean.TYPE_VIDEO ?
                        R.layout.layout_item_vedio_wonderful : R.layout.layout_item_loading_wonderful);
            }
        };
    }

    public interface WonderfulItemClickListener extends View.OnClickListener {

    }

    public interface WonderfulItemLongClickListener extends View.OnLongClickListener {

    }

    public interface LoadMediaListener {
        void loadMedia(MediaBean bean, ImageView imageView);
    }
}
