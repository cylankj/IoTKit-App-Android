package com.cylan.jiafeigou.base.view;

import android.view.SurfaceView;

/**
 * Created by yzd on 16-12-30.
 */

public interface ViewablePresenter extends JFGPresenter {
    /**
     * 能查看设备摄像头的view,都应该具备静音的能力
     */
    void switchSpeaker();

    void switchMicrophone();

    /**
     * 能查看设备摄像头的view,都应该具备主动查看的能力
     */
    void startViewer();

    /**
     * 能查看设备摄像头的view,都应该具备主动断开的能力
     */
    void dismiss();

    SurfaceView getViewerInstance();
}
