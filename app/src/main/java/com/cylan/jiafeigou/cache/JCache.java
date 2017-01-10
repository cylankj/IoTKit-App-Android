package com.cylan.jiafeigou.cache;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;

import java.util.List;

/**
 * Created by cylan-hunt on 16-9-28.
 */

public class JCache {
    /**
     * 非常坑爹啊，需要客户端记录标记。
     * 注册，忘记密码时候，都需要验证码，页面也换，就有不同的提示语{注册成功}
     * true：注册逻辑
     * false:忘记密码逻辑。
     */
    public static boolean isSmsAction = false;

    @Deprecated
    public static boolean isOnline() {
        return GlobalDataProxy.getInstance().isOnline() && GlobalDataProxy.getInstance().getJfgAccount() != null;
    }

    /**
     * @return
     * @see GlobalDataProxy#getInstance()#getAccountCache()
     */
    @Deprecated
    public static JFGAccount getAccountCache() {

        return GlobalDataProxy.getInstance().getJfgAccount();
    }


    public static LoginAccountBean tmpAccount;

    public static List<TimeZoneBean> timeZoneBeenList;
}