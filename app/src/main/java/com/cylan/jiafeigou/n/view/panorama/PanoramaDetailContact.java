package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/**
 * Created by yanzhendong on 2017/3/16.
 */

public interface PanoramaDetailContact {

    interface View extends JFGView {

        void onDeleteResult(int code);
    }

    interface Presenter extends JFGPresenter<View> {
        void delete(PanoramaAlbumContact.PanoramaItem item, int mode);
    }
}
