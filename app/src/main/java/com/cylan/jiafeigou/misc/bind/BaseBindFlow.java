package com.cylan.jiafeigou.misc.bind;

/**
 * Created by yanzhendong on 2017/3/31.
 */

//只提供核心的 bind 功能
//即:1.setServer 2.setLanaguage 3. setWiFi 4.主动查询设备直到设备上线

public class BaseBindFlow {


    public void perform() {
//        Observable.range(0, 2, Schedulers.io())
//                .map(i -> {
//                    try {
//                        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.Ping().toBytes());
//                        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
//                    } catch (JfgException e) {
//                        e.printStackTrace();
//                    }
//                    return i;
//                })
//                .flatMap(i -> RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class))
//                .filter()
    }


}
