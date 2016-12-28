package com.cylan.jiafeigou.misc.listener;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public interface ILiveStateListener {
    /**
     * 可以通过presenter.getPlayState()来获取当前播放状态.
     */
    void liveStateChange();
}
