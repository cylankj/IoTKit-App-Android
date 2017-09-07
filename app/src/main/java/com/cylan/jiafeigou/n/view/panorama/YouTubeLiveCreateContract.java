package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/**
 * Created by yanzhendong on 2017/9/6.
 */

public interface YouTubeLiveCreateContract {

    interface View extends JFGView {

    }

    interface Presenter extends JFGPresenter<View> {

        void createLiveBroadcast(String title,String description,long startTime,long endTime);
    }

}
