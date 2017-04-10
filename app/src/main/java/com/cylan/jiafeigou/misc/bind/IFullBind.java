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

    /**
     * @param check3G   ：检查是否3G狗
     * @param shortUUID
     * @return
     */
    Observable<UdpConstant.UdpDevicePortrait> getBindObservable(boolean check3G, String shortUUID);

    void clean();

    Observable<Boolean> sendWifiInfo(String uuid, String mac, String ssid, String pwd, int type);

}
