package com.cylan.jiafeigou.widget.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.MotionEvent;

import com.cylan.panorama.CameraParam;

import org.webrtc.videoengine.ViEAndroidGLES20;

/**
 * Created by cylan-hunt on 16-11-30.
 */

public class ViEAndroidGLES20_Ext extends ViEAndroidGLES20 implements VideoViewFactory.IVideoView {
    private VideoViewFactory.InterActListener interActListener;

    public ViEAndroidGLES20_Ext(Context context) {
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
        this.interActListener = interActListener;
        setEventListener(new EventListener() {
            @Override
            public boolean onSingleTap(MotionEvent motionEvent) {
                return interActListener != null && interActListener.onSingleTap(motionEvent.getX(), motionEvent.getY());
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
        return false;
    }

    @Override
    public void release() {
        this.interActListener = null;
    }

    @Override
    public void loadBitmap(Bitmap bitmap) {

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
}
