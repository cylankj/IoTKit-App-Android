package com.cylan.jiafeigou.base.view;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;

/**
 * Created by yzd on 16-12-30.
 */

public interface ViewableView extends JFGView {

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

    int CUSTOM_ERROR_CODE_START = -1000000;
    int BAD_NET_WORK = CUSTOM_ERROR_CODE_START - 1;
    int BAD_FRAME_RATE = CUSTOM_ERROR_CODE_START - 2;//帧率过低
    int STOP_VIERER_BY_SYSTEM = CUSTOM_ERROR_CODE_START - 3;//
}
