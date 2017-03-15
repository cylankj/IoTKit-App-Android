package com.cylan.jiafeigou.widget.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.MotionEvent;

import com.cylan.panorama.CameraParam;
import com.cylan.panorama.Panoramic360View;

/**
 * Created by cylan-hunt on 16-11-30.
 */

public class PanoramicView_Ext extends Panoramic360View implements VideoViewFactory.IVideoView {

    private VideoViewFactory.InterActListener interActListener;

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
    public void setInterActListener(VideoViewFactory.InterActListener interActListener) {
        this.interActListener = interActListener;
        setEventListener(new PanoramaEventListener() {
            @Override
            public void onSingleTap(float v, float v1) {
                if (interActListener != null) interActListener.onSingleTap(v, v1);
            }

            @Override
            public void onSnapshot(Bitmap bitmap, boolean b) {
                if (interActListener != null) interActListener.onSnapshot(bitmap, b);
            }
        });
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
        super.loadImage(bitmap);
    }

    @Override
    public void takeSnapshot() {

    }

    @Override
    public void performTouch() {
        // Obtain MotionEvent object
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f;
        float y = 0.0f;
        // List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        );
        // Dispatch touch event to view
        dispatchTouchEvent(motionEvent);
    }

    @Override
    public void detectOrientationChanged() {
        super.detectOrientationChange();
    }
}
