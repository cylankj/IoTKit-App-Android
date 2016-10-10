package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cylan-hunt on 16-9-30.
 */

public class UpdateFileBean implements Parcelable {
    public String url;
    public String savePath;
    public String version;
    public String fileName;

    public UpdateFileBean() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.savePath);
        dest.writeString(this.version);
        dest.writeString(this.fileName);
    }

    protected UpdateFileBean(Parcel in) {
        this.url = in.readString();
        this.savePath = in.readString();
        this.version = in.readString();
        this.fileName = in.readString();
    }

    public static final Creator<UpdateFileBean> CREATOR = new Creator<UpdateFileBean>() {
        @Override
        public UpdateFileBean createFromParcel(Parcel source) {
            return new UpdateFileBean(source);
        }

        @Override
        public UpdateFileBean[] newArray(int size) {
            return new UpdateFileBean[size];
        }
    };

    @Override
    public String toString() {
        return "UpdateFileBean{" +
                "url='" + url + '\'' +
                ", savePath='" + savePath + '\'' +
                ", version='" + version + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
