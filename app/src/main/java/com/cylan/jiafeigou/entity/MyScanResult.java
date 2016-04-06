package com.cylan.jiafeigou.entity;

import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

public class MyScanResult implements Parcelable {
    public ScanResult scanResult;
    public State connectState = State.DISCONNECTED;

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return this.scanResult.SSID.replaceAll("\"", "").equals(((MyScanResult) o).scanResult.SSID.replaceAll("\"", ""));
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.scanResult, 0);
        dest.writeInt(this.connectState == null ? -1 : this.connectState.ordinal());
    }

    public MyScanResult() {
    }

    protected MyScanResult(Parcel in) {
        this.scanResult = in.readParcelable(ScanResult.class.getClassLoader());
        int tmpConnectState = in.readInt();
        this.connectState = tmpConnectState == -1 ? null : State.values()[tmpConnectState];
    }

    public static final Parcelable.Creator<MyScanResult> CREATOR = new Parcelable.Creator<MyScanResult>() {
        public MyScanResult createFromParcel(Parcel source) {
            return new MyScanResult(source);
        }

        public MyScanResult[] newArray(int size) {
            return new MyScanResult[size];
        }
    };
}
