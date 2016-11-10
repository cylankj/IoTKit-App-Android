package com.cylan.jiafeigou.cache;

import android.util.ArrayMap;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;

import java.util.Map;

/**
 * Created by cylan-hunt on 16-9-28.
 */

public class JCache {

    private static Map<String, Object> simpleMap = new ArrayMap<>();
    public static boolean isOnline = false;

    /**
     * 非常坑爹啊，需要客户端记录标记。
     * 注册，忘记密码时候，都需要验证码，页面也换，就有不同的提示语{注册成功}
     * true：注册逻辑
     * false:忘记密码逻辑。
     */
    public static boolean isSmsAction = false;


    /**
     * 应该是两级缓存
     *
     * @param jfgAccount
     */
    public static void setAccountCache(JFGAccount jfgAccount) {
        simpleMap.put("jfgAccount", jfgAccount);
    }

    public static JFGAccount getAccountCache() {
        return (JFGAccount) simpleMap.get("jfgAccount");
    }

    public static LoginAccountBean tmpAccount;
}
