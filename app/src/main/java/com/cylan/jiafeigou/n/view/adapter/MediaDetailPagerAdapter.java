package com.cylan.jiafeigou.n.view.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.jiafeigou.utils.GlideNetVideoUtils;
import com.cylan.jiafeigou.widget.SimpleProgressBar;
import com.cylan.photoview.PhotoView;

import java.util.List;

/**
 * Created by yzd on 16-12-7.
 */

public class MediaDetailPagerAdapter extends PagerAdapter {

    private List<MediaBean> mMediaBeanList;
    private final int mStartPosition;
    private boolean mFirstLoad = true;
    private OnReadToShow mReadToShow;


    public MediaDetailPagerAdapter(List<MediaBean> mediaBeanList, int startPosition) {
        mMediaBeanList = mediaBeanList;
        mStartPosition = startPosition;
    }

    @Override
    public int getCount() {
        return mMediaBeanList == null ? 0 : mMediaBeanList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View contentView;
        MediaBean bean = mMediaBeanList.get(position);
        ImageView photoView;
        if (bean.msgType == MediaBean.TYPE_VIDEO) {
            contentView = View.inflate(container.getContext(), R.layout.view_video_detail, null);
            ViewHolder holder = new ViewHolder(contentView);
            photoView = holder.mPhotoView;
            contentView.setTag(holder);
            ViewCompat.setTransitionName(photoView, position + JConstant.KEY_SHARED_ELEMENT_TRANSITION_NAME_SUFFIX);
            GlideNetVideoUtils.loadNetVideo(container.getContext(), bean.fileName, photoView, () -> {
                if (mFirstLoad && mReadToShow != null) {
                    mReadToShow.onReady();
                }
                mFirstLoad = false;
            });
        } else {
            photoView = new PhotoView(container.getContext());
            contentView = photoView;
            ViewCompat.setTransitionName(photoView, position + JConstant.KEY_SHARED_ELEMENT_TRANSITION_NAME_SUFFIX);
            Glide.with(container.getContext())
                    .load(bean.fileName)
                    .listener((mFirstLoad && position == mStartPosition) ? mListener : null)
                    .into(photoView);
        }

        container.addView(contentView);
        return contentView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private RequestListener<String, GlideDrawable> mListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            if (mFirstLoad && mReadToShow != null) {
                mReadToShow.onReady();
            }
            mFirstLoad = false;
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            if (mFirstLoad && mReadToShow != null) {
                mReadToShow.onReady();
            }
            mFirstLoad = false;
            return false;
        }
    };

    public void setOnInitFinishListener(OnReadToShow listener) {
        mReadToShow = listener;
    }

    public interface OnReadToShow {
        void onReady();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
    }

    public static class ViewHolder {
        public ImageView mPhotoView;
        public TextureView mSurfaceView;
        public SimpleProgressBar mProgressBar;

        public ViewHolder(View root) {
            mPhotoView = (ImageView) root.findViewById(R.id.view_video_picture);
            mSurfaceView = (TextureView) root.findViewById(R.id.view_media_video_view);
            mProgressBar = (SimpleProgressBar) root.findViewById(R.id.view_media_video_loading_bar);
        }
    }

}
