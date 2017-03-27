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
    public long time;
    public long version;
    public int viewType = 0;

    @Override
    public String toString() {
        return "CamMessageBean{" +
                "msgId=" + id +
                ", alarmMsgs=" + alarmMsg +
                ", sdcardSummary=" + sdcardSummary +
                ", startTime=" + time +
                ", version=" + version +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CamMessageBean that = (CamMessageBean) o;

        if (id != that.id) return false;
        if (time != that.time) return false;
        if (version != that.version) return false;
        if (viewType != that.viewType) return false;
        if (alarmMsg != null ? !alarmMsg.equals(that.alarmMsg) : that.alarmMsg != null)
            return false;
        return sdcardSummary != null ? sdcardSummary.equals(that.sdcardSummary) : that.sdcardSummary == null;

    }

    @Override
    public int hashCode() {
        int result = alarmMsg != null ? alarmMsg.hashCode() : 0;
        result = 31 * result + (sdcardSummary != null ? sdcardSummary.hashCode() : 0);
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (int) (version ^ (version >>> 32));
        result = 31 * result + viewType;
        return result;
    }
}
