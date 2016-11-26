package com.cylan.jiafeigou.cache;

import android.util.ArrayMap;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-9-28.
 */

public class JCache {

    private static Map<String, JFGAccount> simpleMap = new ArrayMap<>();

    /**
     * 非常坑爹啊，需要客户端记录标记。
     * 注册，忘记密码时候，都需要验证码，页面也换，就有不同的提示语{注册成功}
     * true：注册逻辑
     * false:忘记密码逻辑。
     */
    public static boolean isSmsAction = false;


    public static boolean isOnline() {
        return onLineStatus && simpleMap.get("jfgAccount") != null;
    }

    public static boolean onLineStatus = false;

    /**
     * 应该是两级缓存
     *
     * @param jfgAccount
     */
    public static void setAccountCache(JFGAccount jfgAccount) {
        AppLogger.d("setAccountCache: " + new Gson().toJson(jfgAccount));
        simpleMap.put("jfgAccount", jfgAccount);
    }

    public static JFGAccount getAccountCache() {
        return simpleMap.get("jfgAccount");
    }

    public static LoginAccountBean tmpAccount;

    public static List<TimeZoneBean> timeZoneBeenList;
}