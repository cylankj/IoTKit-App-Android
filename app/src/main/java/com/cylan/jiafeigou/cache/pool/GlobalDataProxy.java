package com.cylan.jiafeigou.cache.pool;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public class GlobalDataProxy extends DataSourceManager implements IDataProxy {

    private static GlobalDataProxy instance;

    /**
     * <String(cid),ArrayList<...></>></>
     * 根据账号
     */

    public static GlobalDataProxy getInstance() {
        if (instance == null) {
            synchronized (GlobalDataProxy.class) {
                if (instance == null) instance = new GlobalDataProxy();
            }
        }
        return instance;
    }


    @Override
    public long robotGetDataReq(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) throws JfgException {
        AppLogger.e("未实现");
        return 0L;
    }

}
