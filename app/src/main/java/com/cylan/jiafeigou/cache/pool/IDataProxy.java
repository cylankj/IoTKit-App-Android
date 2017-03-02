package com.cylan.jiafeigou.cache.pool;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.BaseValue;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public interface IDataProxy {


    /**
     * 请求
     *
     * @return long req
     */
    long robotGetDataReq(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) throws JfgException;

}
