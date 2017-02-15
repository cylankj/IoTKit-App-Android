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

    void onResolution(JFGMsgVideoResolution resolution) throws JfgException;

    void onFlowSpeed(int speed);

    String onResolveViewLaunchType();

    void onConnectDeviceTimeOut();
}
