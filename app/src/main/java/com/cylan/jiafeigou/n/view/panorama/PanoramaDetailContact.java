package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.utils.CamWarnGlideURL;

/**
 * Created by yanzhendong on 2017/3/16.
 */

public interface PanoramaDetailContact {

    interface View extends JFGView {

        void onDeleteResult(int code);

        void onReportDeviceError(int i, boolean b);

        void onRefreshConnectionMode(int type);

        void savePicResult(Boolean r);
    }

    interface Presenter extends JFGPresenter<View> {

        void delete(PanoramaAlbumContact.PanoramaItem item, int mode, long version);


        void saveImage(CamWarnGlideURL glideURL, String fileName);

        boolean isSaved(String fileName);
    }
}
