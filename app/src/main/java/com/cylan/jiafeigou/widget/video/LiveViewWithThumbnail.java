package com.cylan.jiafeigou.widget.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
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

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.panorama.Panoramic360View;
import com.cylan.panorama.Panoramic360ViewRS;

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
    private String uuid;
    private volatile boolean isLiveThumbLoadingSuccessful = false;

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

    private Bitmap mLiveThumbBitmap = null;

    private Runnable loadLiveThumbRunnable = new Runnable() {
        @Override
        public void run() {
            String filePath = PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_FILE + uuid);
            post(new Runnable() {
                @Override
                public void run() {
                    imgThumbnail.setVisibility(isNormalView ? VISIBLE : GONE);
                }
            });

            AppLogger.i("load uri: " + filePath);
            if (mLiveThumbBitmap == null || mLiveThumbBitmap.isRecycled()) {
                mLiveThumbBitmap = BitmapFactory.decodeFile(filePath);
            }
            if (mLiveThumbBitmap == null) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        imgThumbnail.setImageResource(R.drawable.default_diagram_mask);
                    }
                });
                AppLogger.i(TAG + ",load live thumb failed,bitmap is null,set default picture");
                return;
            }
            if (isNormalView) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (mLiveThumbBitmap != null && !mLiveThumbBitmap.isRecycled()) {
                            ViewGroup.LayoutParams lp = imgThumbnail.getLayoutParams();
                            if (lp.height != ViewGroup.LayoutParams.MATCH_PARENT
                                    || lp.width != ViewGroup.LayoutParams.MATCH_PARENT) {
                                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                imgThumbnail.setLayoutParams(lp);
                            }
                            if (!imgThumbnail.isShown()) {
                                imgThumbnail.setVisibility(VISIBLE);
                            }
                            imgThumbnail.setImageBitmap(mLiveThumbBitmap);
                        }
                    }
                });

            } else if (videoView != null) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (mLiveThumbBitmap != null && !mLiveThumbBitmap.isRecycled()) {
                            videoView.loadBitmap(mLiveThumbBitmap);
                            imgThumbnail.setVisibility(GONE);
                            imgThumbnail.setImageResource(android.R.color.transparent);
                            AppLogger.w("开始加载全景预览图");
                        }
                    }
                });
            }
        }
    };

    @Override
    public void setThumbnail(Uri glideUrl) {
        HandlerThreadUtils.removeCallbacks(loadLiveThumbRunnable);
        HandlerThreadUtils.post(loadLiveThumbRunnable);
    }

    @Override
    public void setLiveView(VideoViewFactory.IVideoView iVideoView, String uuid) {
        this.uuid = uuid;
        if (iVideoView instanceof Panoramic360ViewRS || iVideoView instanceof Panoramic360View) {
            isNormalView = false;
        } else {
            isNormalView = true;
        }
        this.videoView = iVideoView;
        if (videoView != null) {
            videoView.setInterActListener(listener);
        }
        ((View) videoView).setId("videoView".hashCode());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        addView((View) videoView, 0, lp);
    }

    /**
     * 显示黑色块。
     */
    public void setThumbnail() {
        imgThumbnail.setVisibility(VISIBLE);
        imgThumbnail.setImageResource(0);
        imgThumbnail.setBackgroundColor(Color.BLACK);
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
        if (mLiveThumbBitmap != null && !mLiveThumbBitmap.isRecycled()) {
            mLiveThumbBitmap.recycle();
        }
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

}
