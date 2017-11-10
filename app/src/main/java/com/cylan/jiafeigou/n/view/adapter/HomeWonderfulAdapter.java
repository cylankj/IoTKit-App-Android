package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.WonderGlideURL;
import com.cylan.jiafeigou.utils.WonderGlideVideoThumbURL;

import java.util.List;

/**
 * Created by chen on 6/6/16.
 */


public class HomeWonderfulAdapter extends SuperAdapter<DpMsgDefine.DPWonderItem> {
    private WonderfulItemClickListener deviceItemClickListener;
    private WonderfulItemLongClickListener deviceItemLongClickListener;

    public HomeWonderfulAdapter(Context context, List<DpMsgDefine.DPWonderItem> items,
                                IMulItemViewType<DpMsgDefine.DPWonderItem> mulItemViewType) {
        super(context, items, mulItemViewType);

    }

    public void setWonderfulItemClickListener(WonderfulItemClickListener deviceItemClickListener) {
        this.deviceItemClickListener = deviceItemClickListener;
    }

    public void setWonderfulItemLongClickListener(WonderfulItemLongClickListener deviceItemLongClickListener) {
        this.deviceItemLongClickListener = deviceItemLongClickListener;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, DpMsgDefine.DPWonderItem item) {
        if (viewType < 2) {
            initClickListener(holder, layoutPosition);
            handleState(holder, item);
        } else if (item.msgType == DpMsgDefine.DPWonderItem.TYPE_LOAD) {
            holder.setText(R.id.tv_simple_footer_text, getContext().getString(R.string.PULL_TO_LOAD));
            holder.setVisibility(R.id.v_simple_38line, View.VISIBLE);
        } else if (item.msgType == DpMsgDefine.DPWonderItem.TYPE_NO_MORE) {
            holder.setText(R.id.tv_simple_footer_text, getContext().getString(R.string.Loaded));
            holder.setVisibility(R.id.v_simple_38line, View.GONE);
        }

    }

    private void initClickListener(SuperViewHolder holder, final int layoutPosition) {
        ViewCompat.setTransitionName(holder.getView(R.id.iv_wonderful_item_content),
                String.valueOf(layoutPosition) + JConstant.KEY_SHARED_ELEMENT_TRANSITION_NAME_SUFFIX);
        holder.setOnClickListener(R.id.iv_wonderful_item_content, deviceItemClickListener);
        holder.setVisibility(R.id.tv_wonderful_item_share, getContext().getResources().getBoolean(R.bool.show_share_btn) ? View.VISIBLE : View.INVISIBLE);
        holder.setOnClickListener(R.id.tv_wonderful_item_share, deviceItemClickListener);
        holder.setOnClickListener(R.id.tv_wonderful_item_delete, deviceItemClickListener);
        holder.setOnLongClickListener(R.id.rLayout_wonderful_item_wonder, deviceItemLongClickListener);
    }

    private void handleState(SuperViewHolder holder, DpMsgDefine.DPWonderItem bean) {
        //时间
        holder.setText(R.id.tv_wonderful_item_date, TimeUtils.getWonderTime(bean.version));
        //来自摄像头
        if (TextUtils.isEmpty(bean.place)) {
            holder.setVisibility(R.id.tv_wonderful_item_device_name, View.INVISIBLE);
        } else {
            holder.setText(R.id.tv_wonderful_item_device_name, bean.place);
            holder.setVisibility(R.id.tv_wonderful_item_device_name, View.VISIBLE);
        }
        if (bean.msgType == DpMsgDefine.DPWonderItem.TYPE_PIC) {
            GlideApp.with(getContext()).load(new WonderGlideURL(bean))
                    .placeholder(R.drawable.wonderful_pic_place_holder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) holder.getView(R.id.iv_wonderful_item_content));
        } else if (bean.msgType == DpMsgDefine.DPWonderItem.TYPE_VIDEO) {
            GlideApp.with(getContext()).load(new WonderGlideVideoThumbURL(bean))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.wonderful_pic_place_holder)
                    .into((ImageView) holder.getView(R.id.iv_wonderful_item_content));
        }
    }


    @Override
    protected IMulItemViewType<DpMsgDefine.DPWonderItem> offerMultiItemViewType() {
        return new IMulItemViewType<DpMsgDefine.DPWonderItem>() {
            @Override
            public int getViewTypeCount() {
                return 3;
            }

            @Override
            public int getItemViewType(int position, DpMsgDefine.DPWonderItem mediaBean) {
                return mediaBean.msgType < 2 ? mediaBean.msgType : 2;//0:image view  1:videoView
            }

            @Override
            public int getLayoutId(int viewType) {
                switch (viewType) {
                    case 0:
                        return R.layout.layout_item_picture_wonderful;
                    case 1:
                        return R.layout.layout_item_vedio_wonderful;
                    case 2:
                        return R.layout.layout_item_loading_wonderful;
                    default:
                        return R.layout.layout_item_vedio_wonderful;
                }
            }
        };
    }

    public interface WonderfulItemClickListener extends View.OnClickListener {

    }

    public interface WonderfulItemLongClickListener extends View.OnLongClickListener {

    }

    public interface WonderfulLoadMoreListener {
        void loadMore(int position, DpMsgDefine.DPWonderItem item);
    }

}
