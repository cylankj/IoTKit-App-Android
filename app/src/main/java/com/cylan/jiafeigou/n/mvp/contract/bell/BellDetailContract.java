package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.base.module.JFGDoorBellDevice;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.PropertyView;
import com.cylan.jiafeigou.rx.RxEvent;

import rx.Subscription;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellDetailContract {


    interface View extends PropertyView<JFGDoorBellDevice> {
        void checkResult(RxEvent.CheckDevVersionRsp checkDevVersionRsp);
    }

    interface Presenter extends JFGPresenter {
        //        void saveBellInfo(BeanBellInfo info, int id);
        void updateInfoReq(String uuid, Object value, long id);

        /**
         * 检测固件新版本
         */
        void checkNewVersion(String uuid);

        Subscription checkNewVersionBack();
    }
}

