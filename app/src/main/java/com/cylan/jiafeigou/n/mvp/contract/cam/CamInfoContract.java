package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.rx.RxEvent;

import rx.Subscription;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public interface CamInfoContract {

    interface View extends BaseView<Presenter> {
        void checkDevResult(RxEvent.CheckDevVersionRsp checkDevVersionRsp);
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

        /**
         * 检测是否有新固件
         */
        void checkNewSoftVersion();

        /**
         * 新固件检测回调
         * @return
         */
        Subscription checkNewSoftVersionBack();

    }
}
