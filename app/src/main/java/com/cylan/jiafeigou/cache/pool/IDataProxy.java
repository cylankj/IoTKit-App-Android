package com.cylan.jiafeigou.cache.pool;

import android.util.Pair;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.BaseValue;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public interface IDataProxy {
    /**
     * Map<account+uuid,JFGDevice>
     *
     * @param device
     */
    void cacheDevice(JFGDevice... device);

    void robotGetDataRsp(RobotoGetDataRsp dataRsp);

    /**
     * 删除对应的JFGDevice
     *
     * @param uuid
     * @return
     */
    boolean remove(String uuid);

    /**
     * 内部转换 Map<account+uuid,JFGDevice>
     *
     * @param uuid
     * @return
     */
    JFGDevice fetch(String uuid);

    void cacheShareList(ArrayList<JFGShareListInfo> arrayList);

    /**
     * @param uuid uuid+id
     * @param pair
     */
    void cacheUnread(String uuid, Pair<Integer, BaseValue> pair);

    /**
     * 该设备是否已经被分享给其他用户
     *
     * @param uuid
     * @return
     */
    boolean isDeviceSharedTo(String uuid);

    /**
     * 依赖account
     *
     * @return
     */
    ArrayList<JFGShareListInfo> getShareList();

    /**
     * 更新
     *
     * @param device
     * @return
     */
    boolean updateJFGDevice(JFGDevice device);

    ArrayList<JFGDevice> fetchAll();

    boolean insert(String uuid, BaseValue baseValue);

    /**
     * @param uuid
     * @param baseValue
     * @param sync      同步到服务器
     * @return
     */
    boolean update(String uuid, BaseValue baseValue, boolean sync);

    /**
     * 删除和这个uuid相关的所有数据
     *
     * @param uuid
     * @return
     */
    boolean deleteAll(String uuid);

    boolean deleteJFGDevice(String uuid);

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

    BaseValue fetchLocal(String uuid, long id, boolean topOne);

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
    Pair<Integer, BaseValue> fetchUnreadCount(String uuid, long id) throws JfgException;

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
    long robotGetDataReq(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) throws JfgException;

}
