package com.cylan.jiafeigou.widget;

/**
 * Created by cylan-hunt on 16-12-23.
 */

public interface LiveTimeSetter {
    /**
     * 直播的时间
     *
     * @param state :直播或者历史录像
     * @param time
     */
    void setContent(int state, long time);

    void setVisibility(boolean show);
}