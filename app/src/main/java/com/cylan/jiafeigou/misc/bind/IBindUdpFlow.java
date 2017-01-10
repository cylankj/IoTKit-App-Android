package com.cylan.jiafeigou.misc.bind;

/**
 * Created by cylan-hunt on 16-11-14.
 */

public interface IBindUdpFlow {

    /**
     * ping 和 fping一起开工
     *
     * @param shortUUID :从  ap名臣中提取出来的 uuid, 'DOG-100010' shortUUID:100010;
     */
    void startPingFPing(String shortUUID);

    /**
     * 开始升级狗
     */
    void startUpgrade();

    /**
     * 发送客户端`服务器地址信息`
     */
    void sendServerInfo(String ip, int host);

    /**
     * 发送客户端Locale
     */
    void sendLanguageInfo();

    /**
     * 发送wifi配置信息
     *
     * @param ssid
     * @param pwd
     * @param type
     */
    void sendWifiInfo(String ssid, String pwd, int type);
}

