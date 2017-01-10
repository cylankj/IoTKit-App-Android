package com.cylan.jiafeigou.widget.live;

/**
 * Created by cylan-hunt on 16-12-8.
 */

public interface ILiveControl {

    int STATE_IDLE = -1;
    int STATE_PLAYING = 0;
    int STATE_STOP = 1;
    int STATE_LOADING = 2;
    int STATE_LOADING_FAILED = 3;

    /**
     * 播放
     * 暂停
     * loading
     * 停止loading(带text),只有这个状态带有文字说明.
     * 播放暂停两个状态带有超时隐藏功能.
     *
     * @param state
     */
    void setState(int state, CharSequence content);

    int getState();

    void setAction(Action action);

    interface Action {
        void clickImage(int state);

        void clickText();
    }
}
