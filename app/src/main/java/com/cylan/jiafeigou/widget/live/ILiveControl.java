package com.cylan.jiafeigou.widget.live;

import android.view.View;

/**
 * Created by cylan-hunt on 16-12-8.
 */

public interface ILiveControl {


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

    void setState(int state, CharSequence content, String help);

    int getState();

    void setAction(Action action);

    interface Action {
        void clickImage(View view, int state);

        void clickText(View view);

        void clickHelp(View view);
    }
}
