package com.cylan.jiafeigou.cache;

/**
 * Created by cylan-hunt on 16-9-28.
 */

public class JCache {

    public static boolean isOnline = false;

    /**
     * 非常坑爹啊，需要客户端记录标记。
     * 注册，忘记密码时候，都需要验证码，页面也换，就有不同的提示语{注册成功}
     * true：注册逻辑
     * false:忘记密码逻辑。
     */
    public static boolean isSmsAction = false;
}
