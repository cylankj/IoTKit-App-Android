package com.cylan.jiafeigou.n.mvp.contract.setting;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.dp.DataPoint;

/**
 * Created by yanzhendong on 2017/8/2.
 */

public interface AIRecognitionContact {

    interface View extends JFGView {
        void onDeviceUpdate(DataPoint dataPoint);

    }

    interface Presenter extends JFGPresenter {
        void getObjectDetect();
    }
}
