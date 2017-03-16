package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cylan-hunt on 17-3-15.
 */

public class PAlbumBean implements Parcelable {
    public boolean isDate;
    public long timeInDate;
    public int from;
    public String url;
    public boolean selected;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
