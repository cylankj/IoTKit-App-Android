package com.cylan.jiafeigou.widget.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 17-3-13.
 */

public class LiveViewWithThumbnail extends FrameLayout implements VideoViewFactory.ILiveView {
    private static final String TAG = "LiveViewWithThumbnail";
    private VideoViewFactory.IVideoView videoView;//视屏view
    private FrameLayout standByLayout;//待机
    private ImageView imgThumbnail;//缩略图
    private TextView tvLiveFlow;//流量
    private Subscription subscription;

    public LiveViewWithThumbnail(Context context) {
        this(context, null);
    }

    public LiveViewWithThumbnail(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveViewWithThumbnail(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.layout_live_view_with_thumbnail, this, true);
        imgThumbnail = (ImageView) viewGroup.findViewById(R.id.imgv_live_thumbnail);
        imgThumbnail.setOnClickListener(v -> {//do nothing
        });
        standByLayout = (FrameLayout) viewGroup.findViewById(R.id.fLayout_standby_mode);
        tvLiveFlow = (TextView) viewGroup.findViewById(R.id.tv_live_flow);
    }

    public VideoViewFactory.IVideoView getVideoView() {
        return this.videoView;
    }

    public void performTouch() {
        videoView.performTouch();
    }

    /**
     * 待机模式的view:"已进入待机模式,前往打开"
     */
    public void enableStandbyMode(boolean enable, OnClickListener onClickListener, boolean isShareDevice) {
        //进入待机模式
        if (enable) standByLayout.setVisibility(VISIBLE);
        else {
            standByLayout.setVisibility(GONE);
            return;
        }
        TextView tv = (TextView) standByLayout.findViewById(R.id.lLayout_standby_jump_setting);
        //分享设备显示：已进入待机状态
        if (isShareDevice) {
            tv.setText(getContext().getString(R.string.Tap1_Camera_Video_Standby));
            return;
        }
        //非分享设备显示：已进入待机状态，前往开启，和设置点击事件。跳转到设置页面
        if (onClickListener != null)
            tv.setOnClickListener(onClickListener);
    }

    public boolean isShowStandby() {
        return standByLayout.isShown();
    }

    private boolean isNormalView() {
        return videoView != null && !(videoView instanceof PanoramicView360_Ext);
    }

    @Override
    public void setThumbnail(Context context, String token, Uri glideUrl) {
        Glide.with(context)
                .load(glideUrl)
                .asBitmap()
                .signature(new StringSignature(token))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleLoader(imgThumbnail, videoView, isNormalView()));
    }


    @Override
    public void setThumbnail(Context context, String token, Bitmap bitmap) {
        if (bitmap == null) {
            AppLogger.e("preview bitmap is null");
            return;
        } else Log.d(TAG, "setThumbnail: good");
        imgThumbnail.setVisibility(isNormalView() ? VISIBLE : GONE);
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        subscription = Observable.just(bitmap)
                .subscribeOn(Schedulers.io())
                .map(bMap -> {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bMap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    return stream.toByteArray();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> Glide.with(context)
                                .load(bytes)
                                .asBitmap()
                                .signature(new StringSignature(token))
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(new SimpleLoader(imgThumbnail, videoView, isNormalView())),
                        throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()));
    }

    @Override
    public void setLiveView(VideoViewFactory.IVideoView iVideoView) {
        this.videoView = iVideoView;
        ((View) videoView).setId("videoView".hashCode());
        ViewGroup.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView((View) videoView, 0, lp);
    }

    @Override
    public void updateLayoutParameters(int height, int width) {
        ViewGroup.LayoutParams lp = ((View) videoView).getLayoutParams();
        lp.width = width;
        lp.height = height;
        ((View) videoView).setLayoutParams(lp);
        post(() -> {
            Log.d("1280", "1280?w:" + ((View) videoView).getWidth() + ",h:" + ((View) videoView).getHeight());
        });
    }

    @Override
    public void onLiveStart() {
        if (imgThumbnail.isShown()) imgThumbnail.setVisibility(GONE);
        Log.d(TAG, "onLiveStart");
    }

    @Override
    public void onLiveStop() {
        if (isNormalView())
            imgThumbnail.setVisibility(GONE);
        else {
            imgThumbnail.setVisibility(VISIBLE);
            imgThumbnail.setBackgroundResource(android.R.color.black);
        }
        Log.d(TAG, "onLiveStop");
    }

    @Override
    public void showFlowView(boolean show, String content) {
        tvLiveFlow.setVisibility(show ? VISIBLE : GONE);
        tvLiveFlow.setText(content);
    }

    @Override
    public void detectOrientationChanged(boolean port) {
        FrameLayout.LayoutParams lp = (LayoutParams) tvLiveFlow.getLayoutParams();
        if (port) {
            lp.rightMargin = (int) getResources().getDimension(R.dimen.x14);
            lp.topMargin = (int) getResources().getDimension(R.dimen.x14);
        } else {
            lp.rightMargin = (int) getResources().getDimension(R.dimen.x14);
            lp.topMargin = (int) getResources().getDimension(R.dimen.x54);
        }
        tvLiveFlow.setLayoutParams(lp);
        videoView.detectOrientationChanged();
    }

    @Override
    public void onDestroy() {

    }

    private static class SimpleLoader extends SimpleTarget<Bitmap> {

        private WeakReference<ImageView> imageViewRef;
        private WeakReference<VideoViewFactory.IVideoView> videoViewWeakReference;
        private boolean isNormalView;

        public SimpleLoader(ImageView imageView, VideoViewFactory.IVideoView videoView, boolean isNormalView) {
            imageViewRef = new WeakReference<>(imageView);
            videoViewWeakReference = new WeakReference<>(videoView);
            this.isNormalView = isNormalView;

        }

        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
            if (resource != null && !resource.isRecycled()) {
                if (videoViewWeakReference == null || videoViewWeakReference.get() == null) {
                    return;
                }
                if (imageViewRef == null || imageViewRef.get() == null) {
                    return;
                }
                if (isNormalView) {
                    imageViewRef.get().setVisibility(VISIBLE);
                    imageViewRef.get().setImageBitmap(resource);
                } else {
                    videoViewWeakReference.get().loadBitmap(resource);
                }
            } else {
                Log.d(TAG, "bitmap is null? " + (resource == null));
            }
        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            AppLogger.e("set up thumbnail failed: " + e);
        }

        @Override
        public void onDestroy() {
            Log.d(TAG, "bitmap is onDestroy");
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.d(TAG, "bitmap is onStop");
        }

        @Override
        public void onLoadCleared(Drawable placeholder) {
            Log.d(TAG, "bitmap is onLoadCleared");
        }
    }
}
