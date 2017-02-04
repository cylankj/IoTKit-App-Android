package com.cylan.jiafeigou.support;

/**
 * Created by cylan-hunt on 17-2-3.
 */

public class Security {
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * @param packageNameSuffix
     * @return: qq-sdk app key
     */
    public native static String getQQKey(String packageNameSuffix);

    /**
     * @param packageNameSuffix
     * @return :mta统计appKey
     */
    public native static String getMtaKey(String packageNameSuffix);

    /**
     * @param packageNameSuffix
     * @return 微信分享 appKey
     */
    public native static String getWeChatKey(String packageNameSuffix);

    /**
     * @param packageNameSuffix
     * @return :服务器appKey
     */
    public native static String getServerPrefix(String packageNameSuffix);

    /**
     * @param packageNameSuffix
     * @return :服务器appKey
     */
    public native static int getServerPort(String packageNameSuffix);

    /**
     * @param packageNameSuffix
     * @return vKey
     */
    public native static String getVKey(String packageNameSuffix);

    /**
     * @param packageNameSuffix
     * @return vId
     */
    public native static String getVId(String packageNameSuffix);

    public native static String getSinaAppKey(String packageNameSuffix);
}
