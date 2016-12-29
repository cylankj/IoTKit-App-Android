package com.cylan.jiafeigou.dp;

import android.util.Pair;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public interface IDataPoint {

    boolean insert(String uuid, BaseValue baseValue);

    boolean update(String uuid, BaseValue baseValue);

    /**
     * 删除和这个uuid相关的所有数据
     *
     * @param uuid
     * @return
     */
    boolean deleteAll(String uuid);

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

    boolean deleteAll(String uuid, long id, ArrayList<Long> versions);

    /**
     * * 拉取本地数据
     *
     * @param uuid
     * @param id
     * @return
     */
    ArrayList<BaseValue> fetchLocalList(String uuid, long id);

    boolean isSetType(long id);

    /**
     * 未读消息个数
     *
     * @param uuid
     * @param id
     * @return
     */
    Pair<Long, Long> fetchUnreadCount(String uuid, long id) throws JfgException;

    /**
     * 消息已经读
     *
     * @param uuid
     * @param id
     * @return
     */
    boolean markAsRead(String uuid, long id) throws JfgException;

    /**
     * 请求
     *
     * @return long req
     */
    long robotGetData(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) throws JfgException;
}
