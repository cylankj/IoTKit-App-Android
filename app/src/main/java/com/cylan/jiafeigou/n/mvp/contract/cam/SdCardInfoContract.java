package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public interface SdCardInfoContract {

    interface View extends BaseView<Presenter> {

        void sdUseDetail(String volume, float data);

        void showLoading();

        void hideLoading();

        void clearSdResult(int code);

        void initSdUseDetail(DpMsgDefine.DPSdStatus sdStatus);
    }


    interface Presenter extends BasePresenter {

        boolean getSdcardState();

        <T extends DataPoint> void updateInfoReq(T value, long id);

        void clearCountTime();

        Subscription onClearSdReqBack();

        /**
         * 请求指令发送成功后等待结果
         *
         * @return
         */
        Subscription onClearSdResult();

        void getSdCapacity(String uuid);

        Subscription getSdCapacityBack();

        void getClearSdResult();

        Subscription getClearSdResultBack();
    }
}
