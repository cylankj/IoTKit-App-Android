package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.PropertyView;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.rx.RxEvent;

import rx.Subscription;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellDetailContract {


    interface View extends PropertyView {
        void checkResult(RxEvent.CheckVersionRsp checkDevVersionRsp);
    }

    interface Presenter extends JFGPresenter<View> {
        //        void saveBellInfo(BeanBellInfo info, int msgId);
        <T extends DataPoint> void updateInfoReq(String uuid, T value, long id);

        /**
         * 检测固件新版本
         */
        void checkNewVersion(String uuid);

        Subscription checkNewVersionBack();
    }
}

