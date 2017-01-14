package com.cylan.jiafeigou.dp;

import com.cylan.jiafeigou.base.module.JFGDevice;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-11-17.
 * @deprecated
 */

public interface IFlat {

    /**
     * 保存uuid
     *
     * @param account
     * @param uuid
     */
    void cache(String account, String uuid);

    void cache(String account, BaseBean dpDevice);

    void cacheJFGDevice(String account, JFGDevice device);

    /**
     * 缓存报警消息
     *
     * @param account
     * @param uuid
     * @param jfgdpMsgs
     */
    void cache(String account, String uuid, ArrayList<DpMsgDefine.DpMsg> jfgdpMsgs);

    /**
     * 删除账户消息
     *
     * @param account
     */
    void rm(String account);

    /**
     * 删除账户下,某个uuid的消息
     *
     * @param account
     * @param uuid
     */
    void rm(String account, String uuid);

    /**
     * 缓存清空
     */
    void clean();

    void update(String account, String uuid, DpMsgDefine.DpMsg dpMsg);

    /**
     * 获取当前账号下的uuidList
     *
     * @return
     */
    ArrayList<String> getUuidList(String account);

    DpMsgDefine.DpWrap removeMsg(String account, String uuid);

    DpMsgDefine.DpWrap getDevice(String account, String uuid);



    void cache(String account, String uuid, DpMsgDefine.DpMsg msg);

    ArrayList<DpMsgDefine.DpWrap> getAllDevices(String account);


}
