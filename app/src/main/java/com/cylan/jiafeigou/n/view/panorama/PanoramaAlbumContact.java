package com.cylan.jiafeigou.n.view.panorama;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.lzy.okserver.download.DownloadInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumContact.PanoramaItem.PANORAMA_ITEM_TYPE.TYPE_PICTURE;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumContact.PanoramaItem.PANORAMA_ITEM_TYPE.TYPE_VIDEO;

/**
 * Created by yanzhendong on 2017/3/13.
 */

public interface PanoramaAlbumContact {

    interface View extends JFGView {

        void onSyncFinish();

        void onDelete(int position);

        @IntDef
        @Retention(RetentionPolicy.SOURCE)
        @interface ALBUM_VIEW_MODE {
            int MODE_NONE = -1;
            int MODE_PHOTO = 0;
            int MODE_PANORAMA = 1;
            int MODE_BOTH = 2;
        }

        void onAppend(List<PanoramaItem> list, boolean isRefresh, boolean loadFinish, int fetchLocation);

        void onDelete(List<PanoramaItem> positionList);

        void onUpdate(PanoramaItem needUpdate, int position);

        List<PanoramaItem> getList();

        void onViewModeChanged(int mode, boolean report);

        void onSDCardCheckResult(int has_sdcard);
    }

    class PanoramaItem implements Parcelable {
        @IntDef({TYPE_PICTURE, TYPE_VIDEO})
        public @interface PANORAMA_ITEM_TYPE {
            int TYPE_PICTURE = 1;
            int TYPE_VIDEO = 2;
            int TYPE_SEPARATOR = 3;
            int TYPE_MESSAGE_PICTURE = 4;
        }

        public String fileName;
        public int type;//1:jpg,2:mp4,3:分隔符
        public int time;
        public int duration;//如果 type 为1,duration为视频时长
        public boolean selected;
        public DownloadInfo downloadInfo;
        public int location = -1; //0:本地;1:设备;2:本地+设备

        public boolean message = false;

        public PanoramaItem(String name) {
            if (TextUtils.isEmpty(name)) {
                return;
            }
            String[] split = name.split("\\.");
            type = TextUtils.equals("mp4", split[1]) ? PANORAMA_ITEM_TYPE.TYPE_VIDEO : PANORAMA_ITEM_TYPE.TYPE_PICTURE;
            if (type == PANORAMA_ITEM_TYPE.TYPE_PICTURE) {
                time = Integer.parseInt(split[0].split("_")[0]);
            } else if (type == PANORAMA_ITEM_TYPE.TYPE_VIDEO) {
                String[] strings = split[0].split("_");
                time = Integer.parseInt(strings[0]);
                duration = Integer.parseInt(strings[1]);
            }
            fileName = name;
        }

        public PanoramaItem(int time) {
            this.type = PANORAMA_ITEM_TYPE.TYPE_SEPARATOR;
            this.time = time;
        }

        public static String getTaskKey(String uuid, String fileName) {
            return uuid + "/images/" + fileName;
        }

        public static String getMessageTaskKey(String uuid, String fileName) {
            return uuid + ":" + fileName;
        }

        public static boolean accept(String account, String uuid, String filePath) {
            return filePath.matches("/" + account + "/" + uuid);
        }

        public static String getThumbUrl(String uuid, PanoramaItem item) {
            String baseUrl = BasePanoramaApiHelper.getInstance().getDeviceIp();
            String thumbUrl = null;
            if (!TextUtils.isEmpty(baseUrl)) {
                thumbUrl = baseUrl + "/thumb/" + item.fileName.split("\\.")[0] + ".thumb";
            }
            AppLogger.d("正在加载缩略图:" + thumbUrl);
            return thumbUrl;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.fileName);
            dest.writeInt(this.type);
            dest.writeInt(this.time);
            dest.writeInt(this.duration);
            dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
        }

        protected PanoramaItem(Parcel in) {
            this.fileName = in.readString();
            this.type = in.readInt();
            this.time = in.readInt();
            this.duration = in.readInt();
            this.selected = in.readByte() != 0;
        }

        public static final Creator<PanoramaItem> CREATOR = new Creator<PanoramaItem>() {
            @Override
            public PanoramaItem createFromParcel(Parcel source) {
                return new PanoramaItem(source);
            }

            @Override
            public PanoramaItem[] newArray(int size) {
                return new PanoramaItem[size];
            }
        };
    }

    interface Presenter extends JFGPresenter {
        /**
         * 刷新列表d
         *
         * @param time
         */
        void fetch(int time, int fetchLocation);

        void deletePanoramaItem(List<PanoramaItem> items, int mode);
    }
}
