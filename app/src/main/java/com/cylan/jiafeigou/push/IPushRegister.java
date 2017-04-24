package com.cylan.jiafeigou.push;

import android.content.Context;

/**
 * 可能接入很多推送.
 * Created by hds on 17-4-21.
 */

public interface IPushRegister {

    /**
     * 登陆成功,就需要注册
     * push message service
     */
    void registerPMS(Context context);

    void releasePMS();
}
