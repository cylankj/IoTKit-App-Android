package com.cylan.jiafeigou.base.view;

import android.support.annotation.IntDef;
import android.view.SurfaceView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by yzd on 16-12-30.
 */

public interface ViewablePresenter extends JFGPresenter {
    /**
     * 能查看设备摄像头的view,都应该具备静音的能力
     */
    void switchSpeaker();

    void switchMicrophone();

    @IntDef({0, 1})
    @Retention(RetentionPolicy.SOURCE)
    @interface AudioType {
    }

    boolean checkAudio(@AudioType int type);//0: speaker,1: microphone

    /**
     * 能查看设备摄像头的view,都应该具备主动查看的能力
     */
    void startViewer();

    void cancelViewer();

    /**
     * 能查看设备摄像头的view,都应该具备主动断开的能力
     */
    void dismiss();

    SurfaceView getViewerInstance();
}
