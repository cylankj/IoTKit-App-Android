package com.cylan.jiafeigou.misc.bind;

import rx.Observable;

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

    Observable<UdpConstant.UdpDevicePortrait> getBindObservable(String shortUUID);

    void clean();


}
