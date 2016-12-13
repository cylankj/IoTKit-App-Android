package com.cylan.jiafeigou.widget.video;

import android.content.Context;
import android.graphics.Bitmap;

import com.cylan.panorama.CameraParam;

import org.webrtc.videoengine.ViEAndroidGLES20;

/**
 * Created by cylan-hunt on 16-11-30.
 */

public class VideoViewFactory {

    public interface IVideoView {
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
        void release();

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
    }

    public interface InterActListener {
        // 单击
        boolean onSingleTap(float x, float y);

        // opengl截图，回调
        void onSnapshot(Bitmap bitmap, boolean tag);
    }

    public static IVideoView CreateRenderer(Context context) {
        return CreateRendererExt(false, context, false);
    }

    public static IVideoView CreateRendererExt(boolean isPanoramicView, Context context, boolean useOpenGLES2) {
        if (isPanoramicView) {
//            return
            return new PanoramicView_Ext(context);
        }
        return (useOpenGLES2 && ViEAndroidGLES20.IsSupported(context)
                ? new ViEAndroidGLES20_Ext(context)
                : new SurfaceView_Ext(context));
    }
}