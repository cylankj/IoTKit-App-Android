package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/**
 * Created by yanzhendong on 2017/3/11.
 */

public interface PanoramaSettingContact {

    interface View extends JFGView {

        void unbindDeviceRsp(int resultCode);

        void attributeUpdate();
    }

    interface Presenter extends JFGPresenter<View> {

        void unBindDevice();
    }

}
