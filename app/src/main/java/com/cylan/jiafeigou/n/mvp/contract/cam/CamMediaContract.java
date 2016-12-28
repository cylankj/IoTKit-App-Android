package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public interface CamMediaContract {

    interface View extends BaseView<Presenter> {
        void savePic(boolean state);
    }

    interface Presenter extends BasePresenter {
        void saveImage(String url);

        void collect(long time);
    }
}
