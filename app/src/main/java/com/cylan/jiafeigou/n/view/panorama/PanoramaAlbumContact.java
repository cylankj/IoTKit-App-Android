package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.module.PanoramaEvent;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.n.mvp.model.PAlbumBean;

import java.util.ArrayList;

/**
 * Created by yanzhendong on 2017/3/13.
 */

public interface PanoramaAlbumContact {

    interface View extends JFGView {
        enum ALBUM_VIEW_MODE {
            MODE_BOTH, MODE_PANORAMA, MODE_PHOTO;
        }

        void onAppend(ArrayList<PAlbumBean> list);

        void onDelete(ArrayList<PAlbumBean> positionList);

        void onUpdate(PanoramaEvent.MsgFile needUpdate, int position);

        ArrayList<PAlbumBean> getList();
    }

    interface Presenter extends JFGPresenter {
        /**
         * 刷新列表d
         *
         * @param time
         * @param asc
         */
        void fresh(int time, boolean asc);

        /**
         * 手动下载
         */
        void downloadFile(String fileName);
    }
}
