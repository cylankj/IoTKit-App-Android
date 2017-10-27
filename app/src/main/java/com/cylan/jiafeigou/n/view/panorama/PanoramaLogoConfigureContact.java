package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/**
 * Created by yanzhendong on 2017/3/15.
 */

public interface PanoramaLogoConfigureContact {

    interface View extends JFGView {

        class LogoItem {
            public int logoResId = 0;//0:无水印,1:white,2:black,3:cloveDog,-1:自定义
            public int logoType;
            public String resPath = null;

            public LogoItem(int logoResId) {
                this.logoResId = logoResId;
            }

        }

        void onHttpConnectionToDeviceError();

        void onChangeLogoTypeSuccess(int logtype);

        void onChangeLogoTypeError(int position);
    }

    interface Presenter extends JFGPresenter {

        void changeLogoType(int position);
    }
}
