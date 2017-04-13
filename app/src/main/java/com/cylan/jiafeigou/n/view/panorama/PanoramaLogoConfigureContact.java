package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/**
 * Created by yanzhendong on 2017/3/15.
 */

public interface PanoramaLogoConfigureContact {

    interface View extends JFGView {
        class LogoItem {
            public int type = 0;//0:无水印,1:white,2:black,3:cloveDog,-1:自定义
            public String resPath = null;

            public LogoItem(int type) {
                this.type = type;
            }
        }

    }

    interface Presenter extends JFGPresenter<View> {

    }
}
