package com.cylan.jiafeigou.activity.efamily.main;

import com.cylan.jiafeigou.entity.msg.EfamlMsg;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-12-09
 * Time: 15:05
 */

public interface BottomMenuListener {
    void dismiss();

    void show();

    void sendword(EfamlMsg bean);
}
