package com.cylan.jiafeigou.n.mvp.model.param;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.dp.DpMsgMap;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public abstract class BaseParam {
//    public String uuid;
//    public String sn;
//    public String alias;
//    /**
//     * 是否属于分享账号
//     */
//    public String shareAccount;
//    public int pid;
//
//    //--------------------//
//
//    public String mac;
//
//    public DpMsgDefine.DPNet net;
//
//    public String version = "";
//
//    public String sysVersion = "";
//
//    public boolean charging;
//    public int battery;
//
//    public DpMsgDefine.DPTimeZone timeZone;
//
//    public boolean micSwitch;
//    public boolean speakerSwitch;

    /**
     * 一种设备独有的属性
     */
    public abstract ArrayList<JFGDPMsg> queryParameters(Map<Integer, Long> mapVersion);

    protected ArrayList<JFGDPMsg> getBaseList(Map<Integer, Long> mapVersion) {
        ArrayList<JFGDPMsg> baseList = new ArrayList<>();
        baseList.add(new JFGDPMsg(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.MAC_202), getVersion(mapVersion, 202)));
        baseList.add(new JFGDPMsg(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_VERSION_207), getVersion(mapVersion, 207)));
        baseList.add(new JFGDPMsg(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_SYS_VERSION_208), getVersion(mapVersion, 208)));
        baseList.add(new JFGDPMsg(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CHARGING_205), getVersion(mapVersion, 205)));
        return baseList;
    }

    protected long getVersion(Map<Integer, Long> map, int msgId) {
        return map != null ? (map.containsKey(msgId) ? map.get(msgId) : 0L) : 0L;
    }
}
