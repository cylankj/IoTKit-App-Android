package com.cylan.jiafeigou.n.view.panorama;

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

        void onUpdate(PAlbumBean needUpdate, int position);

        ArrayList<PAlbumBean> getList();

        void onDisconnected();

        void onConnected();

        /**
         * 文件损坏，文件不存
         *
         * @param state
         */
        void onFileState(int state);
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
    }
}
