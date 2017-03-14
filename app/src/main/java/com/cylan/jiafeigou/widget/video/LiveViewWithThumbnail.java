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

/**
 * Created by cylan-hunt on 17-3-13.
 */

public class LiveViewWithThumbnail extends FrameLayout implements VideoViewFactory.ILiveView {
    private static final String TAG = "LiveViewWithThumbnail";
    private VideoViewFactory.IVideoView videoView;//视屏view
    private FrameLayout standByLayout;//待机
    private ImageView imgThumbnail;//缩略图
    private TextView tvLiveFlow;//流量

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

    private boolean isNormalView() {
        return videoView != null && !(videoView instanceof PanoramicView_Ext);
    }

    @Override
    public void setThumbnail(Context context, String token, Uri glideUrl) {
        Glide.with(context)
                .load(glideUrl)
                .asBitmap()
                .signature(new StringSignature(token))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (resource != null && !resource.isRecycled()) {
                            if (isNormalView()) {
                                imgThumbnail.setVisibility(VISIBLE);
                                imgThumbnail.setImageBitmap(resource);
                            } else {
                                videoView.loadBitmap(resource);
                            }
                        } else {
                            Log.d(TAG, "bitmap is null? " + (resource == null));
                        }
                    }
                });
    }


    @Override
    public void setThumbnail(Context context, String token, Bitmap bitmap) {
        if (bitmap == null) {
            AppLogger.e("preview bitmap is null");
            return;
        } else Log.d(TAG, "setThumbnail: good");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        imgThumbnail.setVisibility(isNormalView() ? VISIBLE : GONE);
        Glide.with(context)
                .load(stream.toByteArray())
                .asBitmap()
                .signature(new StringSignature(token))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (resource != null && !resource.isRecycled()) {
                            if (isNormalView()) {
                                imgThumbnail.setVisibility(VISIBLE);
                                imgThumbnail.setImageBitmap(resource);
                            } else {
                                videoView.loadBitmap(resource);
                            }
                        } else {
                            Log.d(TAG, "bitmap is null? " + (resource == null));
                        }
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        AppLogger.e("set up thumbnail failed: " + e.getLocalizedMessage());
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
                });
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
    }

}
