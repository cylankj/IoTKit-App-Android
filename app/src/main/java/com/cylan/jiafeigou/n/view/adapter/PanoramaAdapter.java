package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.PAlbumBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cylan-hunt on 17-3-15.
 */

public class PanoramaAdapter extends SuperAdapter<PAlbumBean> {


    private boolean isInEditMode;
    private static final Object object = new Object();
    private List<PAlbumBean> mRemovedList = new ArrayList<>();

    public PanoramaAdapter(Context context, List<PAlbumBean> items, IMulItemViewType<PAlbumBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }


    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, PAlbumBean item) {
        switch (viewType) {
            case 0:
                holder.setText(R.id.tv_cam_message_item_date, "12:30");
                holder.setVisibility(R.id.v_circle, isInEditMode ? View.INVISIBLE : View.VISIBLE);
                break;
            default:
                handleImage(holder, item, layoutPosition);
                holder.setVisibility(R.id.rbtn_item_check, !isInEditMode ? View.INVISIBLE : View.VISIBLE);
                holder.setChecked(R.id.rbtn_item_check, item.selected);
                break;
        }

    }

    public void setInEditMode(boolean inEditMode) {
        isInEditMode = inEditMode;
        notifyDataSetChanged();
    }

    public boolean isInEditMode() {
        return isInEditMode;
    }

    /**
     * @param lastVisiblePosition
     */
    public void reverseEdition(final boolean selected, final int lastVisiblePosition) {
        synchronized (object) {
            for (int i = 0; i < getCount(); i++) {
                PAlbumBean bean = getItem(i);
                if (bean.selected == selected) {
                    bean.selected = !selected;
                    if (i <= lastVisiblePosition)
                        notifyItemChanged(i);
                }
            }
        }
    }

    /**
     * @param lastVisiblePosition
     */
    public void selectAll(final int lastVisiblePosition) {
        synchronized (object) {
            for (int i = 0; i < getCount(); i++) {
                PAlbumBean bean = getItem(i);
                if (bean.selected)
                    continue;
                bean.selected = true;
                if (i <= lastVisiblePosition)
                    notifyItemChanged(i);
            }
        }
    }

    /**
     * @param lastVisiblePosition
     */
    public void selectNone(final int lastVisiblePosition) {
        synchronized (object) {
            for (int i = 0; i < getCount(); i++) {
                PAlbumBean bean = getItem(i);
                if (!bean.selected)
                    continue;
                bean.selected = false;
                if (i <= lastVisiblePosition)
                    notifyItemChanged(i);
            }
        }
    }

    public void remove() {
        synchronized (object) {
            mRemovedList.clear();
            for (int i = getCount() - 1; i >= 0; i--) {
                PAlbumBean bean = getItem(i);
                if (!bean.selected)
                    continue;
                mRemovedList.add(bean);
                remove(i);

            }
        }
    }

    public void reverseItemSelectedState(final int position) {
        synchronized (object) {
            PAlbumBean bean = getItem(position);
            if (bean == null) {
                AppLogger.d("bean is null");
                return;
            }
            bean.selected = !bean.selected;
            notifyItemChanged(position);
        }
    }

    private void handleImage(SuperViewHolder holder, PAlbumBean item, int position) {
        Glide.with(getContext())
                .load(item.url)
                .error(R.drawable.wonderful_pic_place_holder)
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        ((ImageView) holder.getView(R.id.img_album_content)).setImageResource(-1);
                        holder.getView(R.id.img_album_content).setBackground(resource);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        holder.getView(R.id.img_album_content).setBackground(errorDrawable);
                    }
                });
    }

    @Override
    protected IMulItemViewType<PAlbumBean> offerMultiItemViewType() {
        return new IMulItemViewType<PAlbumBean>() {
            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position, PAlbumBean pAlbumBean) {
                return pAlbumBean.isDate ? 0 : 1;
            }

            @Override
            public int getLayoutId(int viewType) {
                return viewType == 1 ? R.layout.layout_panorama_album_item_image :
                        R.layout.layout_panorama_album_item_date;
            }
        };
    }

}

