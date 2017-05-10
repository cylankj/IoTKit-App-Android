package com.cylan.jiafeigou.n.view.panorama;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.BaseHttpApiHelper;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by yanzhendong on 2017/3/13.
 */

public interface PanoramaAlbumContact {

    interface View extends JFGView {
        @IntDef
        @Retention(RetentionPolicy.SOURCE)
        @interface ALBUM_VIEW_MODE {
            int MODE_BOTH = 0;
            int MODE_PANORAMA = 1;
            int MODE_PHOTO = 2;
        }

        void onAppend(List<PanoramaItem> list, boolean isRefresh);

        void onDelete(List<PanoramaItem> positionList);

        void onUpdate(PanoramaItem needUpdate, int position);

        List<PanoramaItem> getList();
    }

    class PanoramaItem {
        public String fileName;
        public int type;//0:jpg,1:mp4,2:分隔符
        public int time;
        public int duration;//如果 type 为1,duration为视频时长
        public boolean selected;

        public PanoramaItem(String name) {
            String[] split = name.split("\\.");
            type = TextUtils.equals("mp4", split[1]) ? 1 : 0;
            if (type == 0) {
                time = Integer.parseInt(split[0]);
            } else if (type == 1) {
                String[] strings = split[0].split("_");
                time = Integer.parseInt(strings[0]);
                duration = Integer.parseInt(strings[1]);
            }
            fileName = name;
        }

        public PanoramaItem(int time) {
            this.type = 2;
            this.time = time;
        }

        public static String getThumbUrl(String uuid, PanoramaItem item) {
            String baseUrl = BaseHttpApiHelper.getInstance().getBaseUrl(uuid, null);
            String thumbUrl = null;
            if (!TextUtils.isEmpty(baseUrl)) {
                thumbUrl = item.type == 0 ? baseUrl + "/images/" + item.fileName : baseUrl + "/thumb/" + item.fileName.replaceAll("mp4", "thumb");
            }
            return thumbUrl;
        }
    }

    interface Presenter extends JFGPresenter<View> {
        /**
         * 刷新列表d
         *
         * @param asc
         */
        void refresh(boolean asc);

        /**
         * 手动下载
         */
        void downloadFile(String fileName);

        void deletePanoramaItem(List<PanoramaItem> items);
    }
}
