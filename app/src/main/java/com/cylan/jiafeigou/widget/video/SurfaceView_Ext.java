package com.cylan.jiafeigou.widget.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.SurfaceView;

import com.cylan.panorama.CameraParam;

/**
 * Created by cylan-hunt on 16-11-30.
 */

public class SurfaceView_Ext extends SurfaceView implements VideoViewFactory.IVideoView {

    public SurfaceView_Ext(Context context) {
        super(context);
    }

    public SurfaceView_Ext(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SurfaceView_Ext(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void config360(CameraParam param) {

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
}
