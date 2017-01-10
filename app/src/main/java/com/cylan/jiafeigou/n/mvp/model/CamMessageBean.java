package com.cylan.jiafeigou.n.mvp.model;

import com.cylan.jiafeigou.dp.DpMsgDefine;

/**
 * Created by hunt on 16-5-14.
 */
public class CamMessageBean {

    public long id = 0;
    /**
     * 直接类型，不需要转型。
     */
    public DpMsgDefine.DPAlarm alarmMsg;
//    public ArrayList<String> urlList;//最终的url
    public DpMsgDefine.DPSdcardSummary content;//204消息
    public long time;
    public long version;

//    public int viewType = 0;

    @Override
    public String toString() {
        return "CamMessageBean{" +
                "id=" + id +
                ", alarmMsgs=" + alarmMsg +
                ", content=" + content +
                ", time=" + time +
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
        return version == that.version;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }
}
