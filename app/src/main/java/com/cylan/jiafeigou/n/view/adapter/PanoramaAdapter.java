package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumContact;
import com.cylan.jiafeigou.support.log.AppLogger;
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

    public PanoramaAdapter(String uuid, Context context, List<PanoramaAlbumContact.PanoramaItem> items) {
        super(context, items, R.layout.layout_panorama_album_item_image);
        this.uuid = uuid;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, PanoramaAlbumContact.PanoramaItem item) {
        boolean isSameDay = false;
        if (layoutPosition > 0) {
            PanoramaAlbumContact.PanoramaItem panoramaItem = getItem(layoutPosition - 1);
            isSameDay = TimeUtils.isSameDay(panoramaItem.time * 1000L, item.time * 1000L);
        }
        if (isSameDay) {
            holder.setVisibility(R.id.tv_cam_message_item_date, View.GONE);
            holder.setVisibility(R.id.v_circle, View.GONE);

        } else {
            holder.setVisibility(R.id.tv_cam_message_item_date, View.VISIBLE);
            holder.setVisibility(R.id.v_circle, isInEditMode ? View.INVISIBLE : View.VISIBLE);
            holder.setText(R.id.tv_cam_message_item_date, TimeUtils.getDayString(item.time * 1000L));
        }
        holder.setVisibility(R.id.fl_check_option, isInEditMode ? View.VISIBLE : View.GONE);
        holder.setVisibility(R.id.dv_time_line, isInEditMode ? View.INVISIBLE : View.VISIBLE);
        holder.setChecked(R.id.rb_item_check, item.selected);
        Glide.with(getContext())
                .load(PanoramaAlbumContact.PanoramaItem.getThumbUrl(uuid, item))
                .error(R.drawable.pic_broken)
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into((ImageView) holder.getView(R.id.img_album_content));
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
}

