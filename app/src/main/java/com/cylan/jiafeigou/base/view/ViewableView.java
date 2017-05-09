package com.cylan.jiafeigou.base.view;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;

/**
 * Created by yzd on 16-12-30.
 */

public interface ViewableView extends JFGView {

    class LiveStreamAction {
        public boolean hasStarted = false;
        public boolean hasResolution = false;
        public boolean hasLiveError = false;
        public boolean speakerOn = false;
        public boolean microphoneOn = false;
        public int liveType = 0;
        public int errorNumber = -1;

        public void reset() {
            hasStarted = false;
            hasResolution = false;
            hasLiveError = false;
            speakerOn = false;
            microphoneOn = false;
            liveType = 0;
            errorNumber = -1;
        }
    }

    void onViewer();//主动查看,不需要点接听

    void onDismiss();//挂断

    void onSpeaker(boolean on);//接听中的麦克风状态改变

    void onMicrophone(boolean on);

    void onResolution(JFGMsgVideoResolution resolution) throws JfgException;

    void onFlowSpeed(int speed);

    String onResolveViewLaunchType();

    void onConnectDeviceTimeOut();

    void onVideoDisconnect(int code);

    void onDeviceUnBind();//当前设备已解绑

    void onLoading(boolean loading);

    void onShowVideoPreviewPicture(String picture);

    int CUSTOM_ERROR_CODE_START = -1000000;
    int BAD_NET_WORK = CUSTOM_ERROR_CODE_START - 1;
    int BAD_FRAME_RATE = CUSTOM_ERROR_CODE_START - 2;//帧率过低
    int STOP_VIERER_BY_SYSTEM = CUSTOM_ERROR_CODE_START - 3;//

    void hasNoAudioPermission();
}
