package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public interface CamWarnContract {

    interface View extends BaseView<Presenter> {


    }

    interface Presenter extends BasePresenter {
//        /**
//         * 刷新BeanCamInfo
//         *
//         * @param info
//         */
//        void saveCamInfoBean(BeanCamInfo info, int dpMsgId);

        /**
         * @param value {@link com.cylan.jiafeigou.dp.BaseValue#setValue(Object)}  }
         * @param id
         */
        <T extends DataPoint> void updateInfoReq(T value, long id);

        void playSound(int id);
    }
}
