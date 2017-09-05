package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import com.cylan.jiafeigou.base.injector.component.DaggerFragmentComponent;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;

import static com.cylan.jiafeigou.n.mvp.model.CamMessageBean.ViewType.FOOT;
import static com.cylan.jiafeigou.n.mvp.model.CamMessageBean.ViewType.ONE_PIC;
import static com.cylan.jiafeigou.n.mvp.model.CamMessageBean.ViewType.TEXT;
import static com.cylan.jiafeigou.n.mvp.model.CamMessageBean.ViewType.THREE_PIC;
import static com.cylan.jiafeigou.n.mvp.model.CamMessageBean.ViewType.TWO_PIC;

/**
 * Created by hunt on 16-5-14.
 */
public class CamMessageBean implements Parcelable {

    /**
     * 直接类型，不需要转型。
     */
    public DpMsgDefine.DPAlarm alarmMsg;
    public DpMsgDefine.DPSdcardSummary sdcardSummary; //204消息
    public DpMsgDefine.DPBellCallRecord bellCallRecord;//401消息
    public long id = 0;
    public long version;
    public @ViewType
    int viewType = ONE_PIC;


    @IntDef({
            FOOT,
            TEXT,
            ONE_PIC,
            TWO_PIC,
            THREE_PIC
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewType {
        int FOOT = 100;
        int TEXT = 200;
        int ONE_PIC = 300;
        int TWO_PIC = 400;
        int THREE_PIC = 500;
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.alarmMsg, flags);
        dest.writeParcelable(this.sdcardSummary, flags);
        dest.writeParcelable(this.bellCallRecord, flags);
        dest.writeLong(this.id);
        dest.writeLong(this.version);
        dest.writeInt(this.viewType);
    }

    public CamMessageBean() {
    }

    protected CamMessageBean(Parcel in) {
        this.alarmMsg = in.readParcelable(DpMsgDefine.DPAlarm.class.getClassLoader());
        this.sdcardSummary = in.readParcelable(DpMsgDefine.DPSdcardSummary.class.getClassLoader());
        this.bellCallRecord = in.readParcelable(DpMsgDefine.DPBellCallRecord.class.getClassLoader());
        this.id = in.readLong();
        this.version = in.readLong();
        this.viewType = in.readInt();
    }

    public static final Creator<CamMessageBean> CREATOR = new Creator<CamMessageBean>() {
        @Override
        public CamMessageBean createFromParcel(Parcel source) {
            return new CamMessageBean(source);
        }

        @Override
        public CamMessageBean[] newArray(int size) {
            return new CamMessageBean[size];
        }
    };
}
