package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public interface CamWarnContract {

    interface View extends BaseView<Presenter> {


    }

    interface Presenter extends BasePresenter {
        /**
         * 刷新BeanCamInfo
         *
         * @param info
         */
        void saveCamInfoBean(BeanCamInfo info, int id);

        BeanCamInfo getBeanCamInfo();
    }
}
