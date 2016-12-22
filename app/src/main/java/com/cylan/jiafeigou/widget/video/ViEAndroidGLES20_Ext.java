package com.cylan.jiafeigou.widget.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.cylan.panorama.CameraParam;

import org.webrtc.videoengine.ViEAndroidGLES20;

/**
 * Created by cylan-hunt on 16-11-30.
 */

public class ViEAndroidGLES20_Ext extends ViEAndroidGLES20 implements VideoViewFactory.IVideoView {

    public ViEAndroidGLES20_Ext(Context context) {
        super(context);
    }


    public ViEAndroidGLES20_Ext(Context context, AttributeSet attr) {
        super(context);
    }

    public ViEAndroidGLES20_Ext(Context context, boolean translucent, int depth, int stencil) {
        super(context, translucent, depth, stencil);
    }

    @Override
    public void config360(CameraParam cameraParam) {

    }

    @Override
    public void setMode(int mode) {

    }


    @Override
    public void setInterActListener(VideoViewFactory.InterActListener interActListener) {

    }

    @Override
    public void config720() {

    }

    @Override
    public boolean isPanoramicView() {
        return false;
    }

    @Override
    public void release() {

    }

    @Override
    public void loadBitmap(Bitmap bitmap) {

    }

    @Override
    public void takeSnapshot() {

    }

    @Override
    public void performTouch() {

    }
}
