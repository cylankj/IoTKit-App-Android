package com.cylan.jiafeigou.misc.listener;

/**
 * Created by cylan-hunt on 16-12-23.
 */

public interface LiveListener {
    /**
     * 播放状态跟踪
     *
     * @param state
     */
    void onLiveState(int state);
}
