package com.cylan.jiafeigou.dp;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public interface IDataPoint {

    boolean insert(String uuid, BaseValue baseValue);

    boolean update(String uuid, BaseValue baseValue);

    Object delete(String uuid, long id);

    /**
     * 可以删HashSet中的元素
     *
     * @param uuid
     * @param id
     * @param version
     * @return
     */
    Object delete(String uuid, long id, long version);

    /**
     * 拉取本地
     *
     * @param uuid
     * @param id
     * @return
     */
    BaseValue fetchLocal(String uuid, long id);

    /**
     * * 拉取本地数据
     *
     * @param uuid
     * @param id
     * @return
     */
    ArrayList<BaseValue> fetchLocalList(String uuid, long id);

    boolean isArrayType(int id);

    /**
     * 请求
     *
     * @return long req
     */
    long robotGetData(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) throws JfgException;
}
