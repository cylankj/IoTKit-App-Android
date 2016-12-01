package com.cylan.jiafeigou.widget.video;

import android.content.Context;
import android.graphics.Bitmap;

import com.cylan.panorama.CameraParam;
import com.cylan.panorama.PanoramicView;

/**
 * Created by cylan-hunt on 16-11-30.
 */

public class PanoramicView_Ext extends PanoramicView implements VideoViewFactory.IVideoView {

    public PanoramicView_Ext(Context context) {
        super(context);
    }

    @Override
    public void config360(CameraParam param) {
        super.configV360(param);
    }

    @Override
    public void setMode(int mode) {

    }

    @Override
    public int getMode() {
        return 0;
    }

    @Override
    public void setInterActListener(VideoViewFactory.InterActListener interActListener) {

    }

    @Override
    public void config720() {

    }

    @Override
    public boolean isPanoramicView() {
        return true;
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
