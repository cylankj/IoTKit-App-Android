package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.JFGCameraDevice;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.rx.RxEvent;

import java.io.IOException;

import rx.Subscription;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public interface CamInfoContract {

    interface View extends BaseView<Presenter> {
        void checkDevResult(RxEvent.CheckDevVersionRsp checkDevVersionRsp);

        void showLoading();

        void hideLoading();

        void clearSdResult(int code);

        void setAliasRsp(int code);

        void deviceUpdate(JFGDPMsg msg) throws IOException;
    }

    interface Presenter extends BasePresenter {
        /**
         * @param value {@link com.cylan.jiafeigou.dp.BaseValue#setValue(Object)}  }
         * @param id
         */
        <T extends DataPoint> void updateInfoReq(T value, long id);

        /**
         * 检测是否有新固件
         */
        void checkNewSoftVersion();

        /**
         * 新固件检测回调
         *
         * @return
         */
        Subscription checkNewSoftVersionBack();

        /**
         * 清空Sd卡
         */
        void clearSdcard();

        /**
         * 发送清空Sd卡请求的回调
         *
         * @return
         */
        Subscription clearSdcardReqBack();


        void updateAlias(Device device);

        Subscription onClearSdReqBack();
    }
}
