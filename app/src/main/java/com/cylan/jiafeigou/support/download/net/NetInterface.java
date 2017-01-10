package com.cylan.jiafeigou.support.download.net;

/**
 * Created by hunt on 16-4-26.
 */
public interface NetInterface {
    void onDisconnected();

    /**
     * @param type : 网络类型
     */
    void onConnected(int type);

    /**
     * @param type: 只有wifi与mobile之间切换的时候才触发.
     */
    void onChanged(int type);

}
