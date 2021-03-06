package com.cylan.jiafeigou.support.zscan.core;

import android.graphics.Rect;

public interface IViewFinder {
    /**
     * Method that executes when Camera preview is starting.
     * It is recommended to setDevice framing rect here and invalidate view after that. <br/>
     * For example see: {@link ViewFinderView#setupViewFinder()}
     */
    void setupViewFinder();

    /**
     * Provides {@link Rect} that identifies area where barcode scanner can detect visual codes
     * <p>Note: This rect is activity_cloud_live_mesg_call_out_item area representation in absolute pixel values. <br/>
     * For example: <br/>
     * If View'account size is 1024x800 so framing rect might be 500x400</p>
     *
     * @return {@link Rect} that identifies barcode scanner area
     */
    Rect getFramingRect();

    /**
     * Width of activity_cloud_live_mesg_call_out_item {@link android.view.View} that implements this interface
     * <p>Note: this is already implemented in {@link android.view.View},
     * so you don't need to override method and provide your implementation</p>
     *
     * @return width of activity_cloud_live_mesg_call_out_item view
     */
    int getWidth();

    /**
     * Height of activity_cloud_live_mesg_call_out_item {@link android.view.View} that implements this interface
     * <p>Note: this is already implemented in {@link android.view.View},
     * so you don't need to override method and provide your implementation</p>
     *
     * @return height of activity_cloud_live_mesg_call_out_item view
     */
    int getHeight();


    /**
     * set something below the scan rect
     * add by hunt
     *
     * @param content
     */
    void setupHint(String content);

    void stop();
}
