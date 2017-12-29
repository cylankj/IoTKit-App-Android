package com.cylan.jiafeigou.widget.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.panorama.CommonPanoramicView;
import com.cylan.panorama.Panoramic360View;
import com.cylan.panorama.Panoramic360ViewRS;

import org.webrtc.videoengine.ViEAndroidGLES20;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

/**
 * Created by cylan-hunt on 17-3-13.
 */

public class LiveViewWithThumbnail extends FrameLayout implements VideoViewFactory.ILiveView {
    private static final String TAG = "LiveViewWithThumbnail";
    private VideoViewFactory.IVideoView videoView;//视屏view
    private FrameLayout standByLayout;//待机
    private ImageView imgThumbnail;//缩略图
    private TextView tvLiveFlow;//流量
    private boolean isNormalView;
//    private Glide glide;

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
            Log.d("wat", "wat");
            if (listener != null) {
                listener.onSingleTap(0, 0);
            }
        });
        standByLayout = (FrameLayout) viewGroup.findViewById(R.id.fLayout_standby_mode);
        tvLiveFlow = (TextView) viewGroup.findViewById(R.id.tv_live_flow);
    }


    public VideoViewFactory.IVideoView getVideoView() {
        return this.videoView;
    }

    public void performTouch() {
        if (videoView instanceof CommonPanoramicView) {
            ((CommonPanoramicView) videoView).onSingleTap(0, 0);
        }
        if (videoView instanceof ViEAndroidGLES20) {
//            ((ViEAndroidGLES20) videoView).onTouch()
            //普通view应该有问题的
        }
    }

    public interface OnSingleTapListener {
        void onSingleTap();
    }

    private VideoViewFactory.InterActListener listener;

    public void setInterActListener(VideoViewFactory.InterActListener listener) {
        this.listener = listener;
        if (videoView != null) {
            videoView.setInterActListener(listener);
        }
    }

    public TextView getTvLiveFlow() {
        return tvLiveFlow;
    }

    /**
     * 待机模式的view:"已进入待机模式,前往打开"
     */
    public void enableStandbyMode(boolean enable, OnClickListener onClickListener, boolean isShareDevice) {
        //进入待机模式
        if (enable) {
            standByLayout.setVisibility(VISIBLE);
            standByLayout.bringToFront();
        } else {
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
        if (onClickListener != null) {
            tv.setOnClickListener(onClickListener);
        }
    }
    private boolean isNormalView() {
        return isNormalView;
    }

    @Override
    public void setThumbnail(Context context, String token, Uri glideUrl) {
        imgThumbnail.setVisibility(isNormalView ? VISIBLE : GONE);
        imgThumbnail.setImageResource(R.drawable.default_diagram_mask);
        AppLogger.i("load uri: " + glideUrl);
        GlideApp.with(context)
                .asBitmap()
                .load(glideUrl)
                .placeholder(R.drawable.default_diagram_mask)
                .signature(new ObjectKey(token))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .into(new SimpleLoader(imgThumbnail, videoView, isNormalView()));
    }


    @Override
    public void setThumbnail(Context context, String token, Bitmap bitmap) {
        imgThumbnail.setVisibility(isNormalView ? VISIBLE : GONE);
        imgThumbnail.setImageResource(R.drawable.default_diagram_mask);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        GlideApp.with(context)
                .asBitmap()
                .load(stream.toByteArray())
                .signature(new ObjectKey(token))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleLoader(imgThumbnail, videoView, isNormalView()));
    }


    /**
     * 显示黑色块。
     */
    public void setThumbnail() {
        imgThumbnail.setVisibility(VISIBLE);
        imgThumbnail.setImageResource(0);
        imgThumbnail.setBackgroundColor(Color.BLACK);
    }

    public void showPreviewThumbnail() {

    }

    @Override
    public void setLiveView(VideoViewFactory.IVideoView iVideoView) {
        if (iVideoView instanceof Panoramic360ViewRS || iVideoView instanceof Panoramic360View) {
            isNormalView = false;
        } else {
            isNormalView = true;
        }
//        isNormalView = !(iVideoView instanceof PanoramicView360_Ext);
        this.videoView = iVideoView;
        if (videoView != null) {
            videoView.setInterActListener(listener);
        }
        ((View) videoView).setId("videoView".hashCode());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        addView((View) videoView, 0, lp);
    }

    @Override
    public void updateLayoutParameters(int height, int width) {
        if (videoView == null) {
            return;
        }
        AppLogger.d("更新view高度: " + height);
        RelativeLayout.LayoutParams parentLp = (RelativeLayout.LayoutParams) getLayoutParams();
        parentLp.height = height;
        setLayoutParams(parentLp);
        ViewGroup.LayoutParams lp = ((View) videoView).getLayoutParams();
        lp.height = height;
        lp.width = width;
        ((View) videoView).setLayoutParams(lp);
    }

    @Override
    public void onCreate(boolean isNormalView) {
        this.isNormalView = isNormalView;
        imgThumbnail.setVisibility(isNormalView ? VISIBLE : GONE);
    }

    @Override
    public void onLiveStart() {
        if (imgThumbnail.isShown()) {
            imgThumbnail.setVisibility(GONE);
        }
        Log.d(TAG, "onLiveStart");
        if (imgThumbnail != null && imgThumbnail.isShown()) {
            imgThumbnail.setVisibility(GONE);
        }
    }

    @Override
    public void onLiveStop() {
        imgThumbnail.setImageResource(android.R.color.transparent);
        imgThumbnail.setVisibility(isNormalView ? VISIBLE : GONE);
        Log.d(TAG, "onLiveStop");
    }

    @Override
    public void showFlowView(boolean show, String content) {
        tvLiveFlow.setVisibility(show ? VISIBLE : GONE);
        tvLiveFlow.setText(content);
    }

    @Override
    public void detectOrientationChanged(boolean port) {
        if (videoView == null) {
            AppLogger.e("这是个bug");
            return;
        }
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

    @Override
    public void showMobileDataInterface(OnClickListener clickListener) {
        final View v = findViewById(R.id.v_mobile_data_cover);
        if (v != null && v.isShown()) {
            return;
        } else if (v != null && !v.isShown()) {
            v.setVisibility(VISIBLE);
            findViewById(R.id.btn_go_ahead)
                    .setOnClickListener(v1 -> {
                        v.setVisibility(GONE);//
                        if (clickListener != null) {
                            clickListener.onClick(v1);
                        }
                    });
            AppLogger.d("显示手机数据,层");
        }
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
        public void onDestroy() {
            AppLogger.w("加载预览图 is onDestroy");
        }

        @Override
        public void onStop() {
            super.onStop();
            AppLogger.w("加载预览图 is stop");
        }

        @Override
        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
            if (resource != null && !resource.isRecycled()) {
                if (videoViewWeakReference == null || videoViewWeakReference.get() == null) {
                    return;
                }
                if (imageViewRef == null || imageViewRef.get() == null) {
                    return;
                }
                if (isNormalView) {
                    ViewGroup.LayoutParams lp = (imageViewRef.get()).getLayoutParams();
                    lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    imageViewRef.get().setLayoutParams(lp);
                    imageViewRef.get().setVisibility(VISIBLE);
                    imageViewRef.get().setImageResource(0);
                    BitmapDrawable bd = new BitmapDrawable(imageViewRef.get().getContext().getResources(), resource);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        imageViewRef.get().setBackground(bd);
                    } else {
                        imageViewRef.get().setBackgroundDrawable(bd);
                    }
                } else {
                    videoViewWeakReference.get().loadBitmap(resource.copy(resource.getConfig(), true));
                    imageViewRef.get().setVisibility(GONE);
                    imageViewRef.get().setImageResource(android.R.color.transparent);
                    AppLogger.w("开始加载全景预览图");
                }
            } else {
                AppLogger.w("开始加载预览图 is null? " + (resource == null));
            }
        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            super.onLoadFailed(errorDrawable);
            if (videoViewWeakReference == null || videoViewWeakReference.get() == null) {
                return;
            }
            if (imageViewRef == null || imageViewRef.get() == null) {
                return;
            }
            imageViewRef.get().setVisibility(VISIBLE);
            imageViewRef.get().setImageBitmap(BitmapFactory.decodeResource(videoViewWeakReference.get().getContext().getResources(),
                    R.drawable.default_diagram_mask));
            AppLogger.w("开始加载全景预览图");
        }

        @Override
        public void onLoadCleared(Drawable placeholder) {
            AppLogger.w("bitmap is onLoadCleared");
        }
    }
}
