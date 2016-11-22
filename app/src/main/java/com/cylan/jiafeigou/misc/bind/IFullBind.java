package com.cylan.jiafeigou.misc.bind;

/**
 * Created by cylan-hunt on 16-11-14.
 */

public interface IFullBind extends IBindUdpFlow {


    /**
     * 开始绑定
     *
     * @param uuid       :设备cid,
     * @param randomCode :随机码
     */
    void startBind(String uuid, String randomCode);

    void clean();


}
