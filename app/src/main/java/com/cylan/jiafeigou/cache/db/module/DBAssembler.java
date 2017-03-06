package com.cylan.jiafeigou.cache.db.module;

import com.cylan.jiafeigou.base.module.DataSourceManager;

/**
 * Created by yanzhendong on 2017/3/4.
 */

public class DBAssembler {

    void ss(){
        DataSourceManager.getInstance().cacheJFGDevices();
    }
}
