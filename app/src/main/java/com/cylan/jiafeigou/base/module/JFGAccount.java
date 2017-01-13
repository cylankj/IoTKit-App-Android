package com.cylan.jiafeigou.base.module;

import android.os.Parcel;

import com.cylan.annotation.DPProperty;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;

/**
 * Created by yzd on 17-1-13.
 */

public class JFGAccount extends DataPoint<JFGAccount> {

    @DPProperty(msgId = DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG)
    public DpMsgDefine.DPSet<DpMsgDefine.DPWonderItem> account_wonderful_msg;

    private com.cylan.entity.jniCall.JFGAccount mAccount;

    public JFGAccount setAccount(com.cylan.entity.jniCall.JFGAccount account) {
        this.mAccount = account;
        this.id = 888080;
        return this;
    }

    public String getPhone() {
        return this.mAccount.getPhone();
    }

    public String getToken() {
        return this.mAccount.getToken();
    }

    public String getAlias() {
        return this.mAccount.getAlias();
    }

    public boolean isEnablePush() {
        return this.mAccount.isEnablePush();
    }

    public boolean isEnableSound() {
        return this.mAccount.isEnableSound();
    }

    public String getEmail() {
        return this.mAccount.getEmail();
    }

    public boolean isEnableVibrate() {
        return this.mAccount.isEnableVibrate();
    }

    public String getPhotoUrl() {
        return this.mAccount.getPhotoUrl();
    }

    public String getAccount() {
        return this.mAccount.getAccount();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeSerializable(this.mAccount);
    }

    public JFGAccount() {
    }

    protected JFGAccount(Parcel in) {
        super(in);
        this.mAccount = (com.cylan.entity.jniCall.JFGAccount) in.readSerializable();
    }

    public static final Creator<JFGAccount> CREATOR = new Creator<JFGAccount>() {
        @Override
        public JFGAccount createFromParcel(Parcel source) {
            return new JFGAccount(source);
        }

        @Override
        public JFGAccount[] newArray(int size) {
            return new JFGAccount[size];
        }
    };
}
