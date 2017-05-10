package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumContact;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cylan-hunt on 17-3-15.
 */

public class PanoramaAdapter extends SuperAdapter<PanoramaAlbumContact.PanoramaItem> {


    private boolean isInEditMode;
    private static final Object object = new Object();
    private List<PanoramaAlbumContact.PanoramaItem> mRemovedList = new ArrayList<>();
    private String uuid;

    public PanoramaAdapter(String uuid, Context context, List<PanoramaAlbumContact.PanoramaItem> items, IMulItemViewType<PanoramaAlbumContact.PanoramaItem> mulItemViewType) {
        super(context, items, mulItemViewType);
        this.uuid = uuid;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, PanoramaAlbumContact.PanoramaItem item) {
        switch (viewType) {
            case 0:
                holder.setText(R.id.tv_cam_message_item_date, TimeUtils.getDayString(item.time * 1000L));
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
                PanoramaAlbumContact.PanoramaItem bean = getItem(i);
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
                PanoramaAlbumContact.PanoramaItem bean = getItem(i);
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
                PanoramaAlbumContact.PanoramaItem bean = getItem(i);
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
                PanoramaAlbumContact.PanoramaItem bean = getItem(i);
                if (!bean.selected)
                    continue;
                mRemovedList.add(bean);
                remove(i);

            }
        }
    }

    public void reverseItemSelectedState(final int position) {
        synchronized (object) {
            PanoramaAlbumContact.PanoramaItem bean = getItem(position);
            if (bean == null) {
                AppLogger.d("bean is null");
                return;
            }
            bean.selected = !bean.selected;
            notifyItemChanged(position);
        }
    }

    private void handleImage(SuperViewHolder holder, PanoramaAlbumContact.PanoramaItem item, int position) {
        Glide.with(getContext())
                .load(PanoramaAlbumContact.PanoramaItem.getThumbUrl(uuid, item))
                .error(R.drawable.pic_broken)
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .into(new ImageViewTarget<GlideDrawable>(holder.getView(R.id.img_album_content)) {
                    @Override
                    protected void setResource(GlideDrawable resource) {
                        view.setBackground(resource);
                    }
                });
    }

    @Override
    protected IMulItemViewType<PanoramaAlbumContact.PanoramaItem> offerMultiItemViewType() {
        return new IMulItemViewType<PanoramaAlbumContact.PanoramaItem>() {
            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position, PanoramaAlbumContact.PanoramaItem pAlbumBean) {
                return pAlbumBean.type == 2 ? 0 : 1;
            }

            @Override
            public int getLayoutId(int viewType) {
                return viewType == 1 ? R.layout.layout_panorama_album_item_image :
                        R.layout.layout_panorama_album_item_date;
            }
        };
    }

}

