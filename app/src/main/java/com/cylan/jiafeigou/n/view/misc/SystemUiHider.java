package com.cylan.jiafeigou.n.view.misc;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by cylan-hunt on 16-7-14.
 */
public class SystemUiHider {

    private WeakReference<View> weakReference;

    public SystemUiHider(View view) {
        weakReference = new WeakReference<>(view);
    }

    public SystemUiHider(View view, boolean isAutoHide) {
        weakReference = new WeakReference<>(view);
        this.supportAutoHide = isAutoHide;
        view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {

            }
        });
    }

    public void setSupportAutoHide(boolean supportAutoHide) {
        this.supportAutoHide = supportAutoHide;
    }

    private boolean supportAutoHide = false;

    private static final long AUTO_HIDE_DELAY = 3000L;
    /**
     * Some older devices needs activity_cloud_live_mesg_call_out_item small delay between UI widget updates
     * and activity_cloud_live_mesg_call_out_item change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler mHideHandler = new Handler();

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-startTime and do nothing on earlier devices.
            if (weakReference == null || weakReference.get() == null) {
                return;
            }
            weakReference.get().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private boolean mVisible;

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    public void hide() {
        // Hide UI first
        mVisible = false;
        // Schedule activity_cloud_live_mesg_call_out_item runnable to remove the status and navigation bar after activity_cloud_live_mesg_call_out_item delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    public void show() {
        // Show the system bar
        if (weakReference == null || weakReference.get() == null) {
            return;
        }
        weakReference.get().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;
        // Schedule activity_cloud_live_mesg_call_out_item runnable to display UI elements after activity_cloud_live_mesg_call_out_item delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        if (supportAutoHide) {
            mHideHandler.postDelayed(mHidePart2Runnable, AUTO_HIDE_DELAY);
        }
    }

    /**
     * Schedules activity_cloud_live_mesg_call_out_item call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    public void delayedHide(long delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void removeDelayRunnable() {
        mHideHandler.removeCallbacksAndMessages(null);
    }

}
