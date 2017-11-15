package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import com.cylan.jiafeigou.dp.BaseDataPoint;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.cylan.jiafeigou.n.mvp.model.CamMessageBean.ViewType.FOOT;
import static com.cylan.jiafeigou.n.mvp.model.CamMessageBean.ViewType.ONE_PIC;
import static com.cylan.jiafeigou.n.mvp.model.CamMessageBean.ViewType.TEXT;
import static com.cylan.jiafeigou.n.mvp.model.CamMessageBean.ViewType.THREE_PIC;
import static com.cylan.jiafeigou.n.mvp.model.CamMessageBean.ViewType.TWO_PIC;

/**
 * Created by hunt on 16-5-14.
 */
public class CamMessageBean implements Parcelable {

//    /**

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CamMessageBean)) return false;

        CamMessageBean that = (CamMessageBean) o;

        if (viewType != that.viewType) return false;
        return message.equals(that.message);
    }

    @Override
    public int hashCode() {
        int result = message.hashCode();
        result = 31 * result + viewType;
        return result;
    }

    //     * 直接类型，不需要转型。
//     */
//    public DpMsgDefine.DPAlarm alarmMsg;
//    public DpMsgDefine.DPSdcardSummary sdcardSummary; //204消息
//    public DpMsgDefine.DPBellCallRecord bellCallRecord;//401消息
    public BaseDataPoint message = EMPTY;
    private static final BaseDataPoint EMPTY = new BaseDataPoint() {
    };
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


    public CamMessageBean() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.message, flags);
        dest.writeInt(this.viewType);
    }

    protected CamMessageBean(Parcel in) {
        this.message = in.readParcelable(BaseDataPoint.class.getClassLoader());
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
