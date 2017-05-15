package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumContact;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.PanoramaThumbURL;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.lzy.okserver.download.DownloadInfo;
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
        holder.setVisibility(R.id.fl_check_option, isInEditMode ? View.VISIBLE : View.GONE);
        holder.setVisibility(R.id.dv_time_line, isInEditMode ? View.INVISIBLE : View.VISIBLE);
        holder.setChecked(R.id.rb_item_check, item.selected);
        holder.setVisibility(R.id.iv_album_video_duration_text, item.type == 1 ? View.VISIBLE : View.GONE);
        holder.setText(R.id.iv_album_video_duration_text, TimeUtils.getMM_SS(item.duration * 1000L));
        holder.setVisibility(R.id.iv_album_icon_720_iphone, (item.location == 0 || item.location == 2) ? View.VISIBLE : View.GONE);
        //0:本地;1:设备;2:本地+设备
        holder.setVisibility(R.id.iv_album_icon_720_camera, (item.location == 1 || item.location == 2) ? View.VISIBLE : View.GONE);
        Glide.with(getContext())
                .load(new PanoramaThumbURL(uuid, item.fileName))
                .error(R.drawable.pic_broken)
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into((ImageView) holder.getView(R.id.img_album_content));
        if (item.downloadInfo == null) {
            return;
        }

        TextView view = holder.getView(R.id.tv_album_download_progress);
        DownloadListener listener = item.downloadInfo.getListener() == null ? new MyDownloadListener() : item.downloadInfo.getListener();
        listener.setUserTag(holder);
        view.setTag(item.downloadInfo);
        view.setText((int) (item.downloadInfo.getProgress() * 100) + "%");
    }

    public static class MyDownloadListener extends DownloadListener {

        @Override
        public void onProgress(DownloadInfo downloadInfo) {
            if (getUserTag() == null) return;
            SuperViewHolder holder = (SuperViewHolder) getUserTag();
            TextView textView = holder.getView(R.id.tv_album_download_progress);
            DownloadInfo info = (DownloadInfo) textView.getTag();
            int percent = (int) (info.getProgress() * 100);
            textView.setText(percent + "%");  //这里不能使用传递进来的 DownloadInfo，否者会出现条目错乱的问题
            if (percent == 100) {
                holder.setVisibility(R.id.iv_album_icon_720_iphone, View.VISIBLE);
            }
        }

        @Override
        public void onFinish(DownloadInfo downloadInfo) {
            if (getUserTag() == null) return;
            SuperViewHolder holder = (SuperViewHolder) getUserTag();
            holder.setVisibility(R.id.iv_album_icon_720_iphone, View.VISIBLE);
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

