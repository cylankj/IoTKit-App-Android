package com.cylan.jiafeigou.widget.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.cylan.jiafeigou.misc.pty.PropertiesLoader;
import com.cylan.panorama.CameraParam;

import org.webrtc.videoengine.ViEAndroidGLES20;

/**
 * Created by cylan-hunt on 16-11-30.
 */

public class VideoViewFactory {

    public interface IVideoView {

        Context getContext();

        /**
         * 全景配置360°
         */
        void config360(CameraParam param);

        /**
         * 设置吊顶,平视
         *
         * @param mode
         */
        void setMode(int mode);

//        int getMode();

        void setInterActListener(InterActListener interActListener);

        /**
         * 全景配置720°
         */
        void config720();

        /**
         * 是否为全景视图
         *
         * @return
         */
        boolean isPanoramicView();

        /**
         * 释放
         */
        void onDestroy();

        void onPause();

        void onResume();

        /**
         * 加载暂停 图片
         *
         * @param bitmap
         */
        void loadBitmap(Bitmap bitmap);

        /**
         * 截图
         */
        void takeSnapshot();

        void performTouch();

        void detectOrientationChanged();

        Bitmap getCacheBitmap();
    }

    public interface ILiveView {

        void setThumbnail(Context context, String token, Uri glideUrl);

        /**
         * @param context ： for glide to perform with life cycle
         * @param bitmap
         */
        void setThumbnail(Context context, String token, Bitmap bitmap);

        void setLiveView(IVideoView iVideoView);

        void updateLayoutParameters(int height, int weight);

        void onCreate(boolean isNormalView);

        void onLiveStart();

        void onLiveStop();

        /**
         * 流量
         */
        void showFlowView(boolean show, String content);

        void detectOrientationChanged(boolean port);

        void onDestroy();

        void showMobileDataInterface(View.OnClickListener clickListener);
    }

    public interface InterActListener {
        // 单击
        boolean onSingleTap(float x, float y);

        // opengl截图，回调
        void onSnapshot(Bitmap bitmap, boolean tag);
    }

    public static IVideoView CreateRenderer(Context context) {
        return CreateRendererExt(false, context, false, false);
    }

    public static IVideoView CreateRendererExt(boolean isPanoramicView, Context context, boolean useOpenGLES2, boolean tank) {
        if (isPanoramicView) {
//            return
            return tank ? new PanoramicView360_Ext(context) : new PanoramicView360_Ext(context);
        }
        return (useOpenGLES2 && ViEAndroidGLES20.IsSupported(context)
                ? new ViEAndroidGLES20_Ext(context)
                : new SurfaceView_Ext(context));
    }

    /**
     * @param pid:normalView 1:PanoramicView 2:鱼缸 view
     */
    public static IVideoView CreateRendererExt(int pid, Context context) {
        PropertiesLoader loader = PropertiesLoader.getInstance();
        String view_mode = loader.property(pid, "VIEW_MODE");
        String view = loader.property(pid, "VIEW");
        if (!TextUtils.isEmpty(view_mode) && TextUtils.equals(view_mode, "1")) {
            return CreateRendererExt(RENDERER_VIEW_TYPE.TYPE_PANORAMA_360_RS, context, true);
        } else if (!TextUtils.isEmpty(view) && view.contains("圆形")) {
            return CreateRendererExt(RENDERER_VIEW_TYPE.TYPE_PANORAMA_360, context, true);
        } else if (!TextUtils.isEmpty(view) && view.contains("鱼缸")) {
            return CreateRendererExt(RENDERER_VIEW_TYPE.TYPE_PANORAMA_360_RS, context, true);
        }
        return CreateRendererExt(RENDERER_VIEW_TYPE.TYPE_DEFAULT, context, true);
    }


//    public static float getRendererVideoRadio(int pid, int expectWidth, int expectHeight, boolean land) {
//        float ratio = isNormalView ? (isLand() ? getLandFillScreen() : (float) resolution.height / resolution.width) :
//                isLand() ? (float) Resources.getSystem().getDisplayMetrics().heightPixels /
//                        Resources.getSystem().getDisplayMetrics().widthPixels : 1.0f;
//    }

    public enum RENDERER_VIEW_TYPE {
        TYPE_PANORAMA_360, TYPE_PANORAMA_720, TYPE_DEFAULT, TYPE_PANORAMA_360_RS
    }

    public static IVideoView CreateRendererExt(RENDERER_VIEW_TYPE view_type, Context context, boolean useOpenGLES2) {
        switch (view_type) {
            case TYPE_PANORAMA_360:
                return new PanoramicView360_Ext(context);
            case TYPE_PANORAMA_720:
                return new PanoramicView720_Ext(context);
            case TYPE_PANORAMA_360_RS:
                return new PanoramicView360RS_Ext(context);
            default:
                return (useOpenGLES2 && ViEAndroidGLES20.IsSupported(context) ? new ViEAndroidGLES20_Ext(context) : new SurfaceView_Ext(context));
        }
    }
}