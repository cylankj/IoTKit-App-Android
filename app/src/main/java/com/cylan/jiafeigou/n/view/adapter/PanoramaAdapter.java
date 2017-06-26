package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumContact;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.PanoramaThumbURL;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.lzy.okserver.download.DownloadInfo;
import com.lzy.okserver.download.DownloadManager;
import com.lzy.okserver.listener.DownloadListener;

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
        ViewGroup container = holder.getView(R.id.lLayout_cam_msg_container);
        int dimension = container.getResources().getDimensionPixelOffset(R.dimen.y15);
        container.setPadding(isInEditMode ? dimension : 0, 0, isInEditMode ? 0 : dimension, 0);
        holder.setVisibility(R.id.dv_time_line, isInEditMode ? View.INVISIBLE : View.VISIBLE);
        holder.setChecked(R.id.rb_item_check, item.selected);
        holder.setVisibility(R.id.rb_item_check, isInEditMode ? View.VISIBLE : View.GONE);
        holder.setVisibility(R.id.iv_album_video_duration_text, item.type == 1 ? View.VISIBLE : View.GONE);
        holder.setText(R.id.iv_album_video_duration_text, TimeUtils.getMM_SS(item.duration * 1000L));
        holder.setVisibility(R.id.iv_album_icon_720_iphone, (item.location == 0 || item.location == 2) ? View.VISIBLE : View.GONE);
        //0:本地;1:设备;2:本地+设备
        holder.setVisibility(R.id.iv_album_icon_720_camera, (item.location == 1 || item.location == 2) ? View.VISIBLE : View.GONE);
        Glide.with(getContext())
                .load(new PanoramaThumbURL(uuid, item.fileName))
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new ImageViewTarget<GlideDrawable>(holder.getView(R.id.img_album_content)) {
                    @Override
                    protected void setResource(GlideDrawable resource) {
                        view.setImageDrawable(resource);
                        view.setScaleType(ImageView.ScaleType.FIT_XY);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        view.setBackgroundResource(R.drawable.wonderful_pic_place_holder);
                        view.setImageResource(R.drawable.pic_broken);
                        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                    }
                });
        TextView view = holder.getView(R.id.tv_album_download_progress);
        if (item.downloadInfo == null) {
            item.downloadInfo = DownloadManager.getInstance().getDownloadInfo(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, item.fileName));//确保真的没有 download 信息
        }
        if (item.downloadInfo == null) {
            AppLogger.e("download is null" + "item type:" + item.type + ",file name:" + item.fileName);
            view.setVisibility(View.INVISIBLE);
            return;
        }

        DownloadListener listener = (DownloadListener) view.getTag(3 << 24 + 1);
        PanoramaAlbumContact.PanoramaItem panoramaItem = (PanoramaAlbumContact.PanoramaItem) view.getTag(3 << 24 + 2);
        if (listener == null) {
            listener = new MyDownloadListener();
            listener.setUserTag(holder);
        }
        if (panoramaItem != null && panoramaItem.downloadInfo != null) {
            panoramaItem.downloadInfo.setListener(null);
        }
        panoramaItem = item;
        panoramaItem.downloadInfo.setListener(listener);
        view.setTag(3 << 24 + 1, listener);
        view.setTag(3 << 24 + 2, panoramaItem);
        int percent = (int) (panoramaItem.downloadInfo.getProgress() * 100);
        view.setText(percent + "%");
        view.setVisibility(percent >= 100 ? View.INVISIBLE : View.VISIBLE);
        holder.setVisibility(R.id.iv_album_icon_720_iphone, percent >= 100 ? View.VISIBLE : View.INVISIBLE);
    }

    public static class MyDownloadListener extends DownloadListener {

        @Override
        public void onProgress(DownloadInfo downloadInfo) {
            if (getUserTag() == null) return;
            SuperViewHolder holder = (SuperViewHolder) getUserTag();
            TextView textView = holder.getView(R.id.tv_album_download_progress);
            textView.setText((int) (downloadInfo.getProgress() * 100) + "%");
        }

        @Override
        public void onFinish(DownloadInfo downloadInfo) {
            if (getUserTag() == null) return;
            SuperViewHolder holder = (SuperViewHolder) getUserTag();
            holder.setVisibility(R.id.iv_album_icon_720_iphone, View.VISIBLE);
            TextView textView = holder.getView(R.id.tv_album_download_progress);
            PanoramaAlbumContact.PanoramaItem panoramaItem = (PanoramaAlbumContact.PanoramaItem) textView.getTag(3 << 24 + 2);
            panoramaItem.location = 2;
            textView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onError(DownloadInfo downloadInfo, String errorMsg, Exception e) {
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

    public List<PanoramaAlbumContact.PanoramaItem> getRemovedList() {
        synchronized (object) {
            mRemovedList.clear();
            for (int i = getCount() - 1; i >= 0; i--) {
                PanoramaAlbumContact.PanoramaItem bean = getItem(i);
                if (!bean.selected)
                    continue;
                mRemovedList.add(bean);

            }
        }
        return mRemovedList;
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

