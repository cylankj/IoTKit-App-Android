package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/**
 * Created by yanzhendong on 2017/3/13.
 */

public interface PanoramaAlbumContact {

    interface View extends JFGView {
        enum ALBUM_VIEW_MODE {
            MODE_BOTH, MODE_PANORAMA, MODE_PHOTO;
        }


    }

    interface Presenter extends JFGPresenter {

    }
}
