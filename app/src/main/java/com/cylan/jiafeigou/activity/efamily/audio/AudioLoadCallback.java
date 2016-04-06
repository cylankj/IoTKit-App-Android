package com.cylan.jiafeigou.activity.efamily.audio;

import com.cylan.jiafeigou.entity.msg.EfamlMsg;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-13
 * Time: 10:24
 */

public interface AudioLoadCallback {
    void start(EfamlMsg mEfamlMsg);

    void stopOther();

    void stopNoFile();
}
