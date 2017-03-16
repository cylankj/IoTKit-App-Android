package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/**
 * Created by yanzhendong on 2017/3/11.
 */

public class PanoramaSettingContact {

    interface View extends JFGView {

        void unbindDeviceRsp(int resultCode);
    }

    interface Presenter extends JFGPresenter {

        void unBindDevice();
    }

}
