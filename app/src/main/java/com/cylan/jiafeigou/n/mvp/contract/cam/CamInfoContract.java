package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public interface CamInfoContract {

    interface View extends BaseView<Presenter> {


    }

    interface Presenter extends BasePresenter {
//        /**
//         * 刷新BeanCamInfo
//         *
//         * @param info
//         */
//        void saveCamInfoBean(BeanCamInfo info, int id);
//
//        BeanCamInfo getBeanCamInfo();

        /**
         * @param value {@link com.cylan.jiafeigou.dp.BaseValue#setValue(Object)}  }
         * @param id
         */
        void updateInfoReq(Object value, long id);

    }
}
