package com.cylan.jiafeigou.misc.bind;

/**
 * Created by cylan-hunt on 16-11-14.
 */

public interface IBindResult {
    /**
     * ping状态没有响应
     */
    void pingFPingFailed();


    /**
     * 3G狗,会省略掉很多步骤.
     */
    void isMobileNet();

    /**
     * 通知ui
     * 设备信息,版本号,mac,全包含在fpong消息里面.
     */
    void needToUpgrade();

    /**
     * 升级状态
     * {@link IBindResult#UPGRADE_FAILED,IBindResult#UPGRADING,IBindResult#UPGRADE_SUCCESS}
     *
     * @param state
     */
    void updateState(int state);

    /**
     * 绑定失败
     */
    void bindFailed();

    /**
     * 绑定成功
     */
    void bindSuccess();

    /**
     * 绑定udp消息流程完成
     */
    void onLocalFlowFinish();

    int UPGRADE_FAILED = -1;
    int UPGRADING = 0;
    int UPGRADE_SUCCESS = 1;
}
