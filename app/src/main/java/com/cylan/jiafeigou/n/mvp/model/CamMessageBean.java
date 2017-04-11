package com.cylan.jiafeigou.n.mvp.model;

import com.cylan.jiafeigou.dp.DpMsgDefine;

/**
 * Created by hunt on 16-5-14.
 */
public class CamMessageBean {

    /**
     * 直接类型，不需要转型。
     */
    public DpMsgDefine.DPAlarm alarmMsg;
    public DpMsgDefine.DPSdcardSummary sdcardSummary;//204消息
    public long id = 0;
    public long version;
    public int viewType = 0;

    @Override
    public String toString() {
        return "CamMessageBean{" +
                "msgId=" + id +
                ", alarmMsgs=" + alarmMsg +
                ", sdcardSummary=" + sdcardSummary +
                ", startversion=" + version +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CamMessageBean that = (CamMessageBean) o;

        if (id != that.id) return false;
        return version == that.version;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }
}
