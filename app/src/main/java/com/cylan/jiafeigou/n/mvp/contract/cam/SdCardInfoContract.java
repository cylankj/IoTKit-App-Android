package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

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

        void initSdUseDetailRsp(DpMsgDefine.DPSdStatus sdStatus, boolean alert);

        void showSdPopDialog();

        void onNetworkChanged(boolean connected);

    }


    interface Presenter extends BasePresenter {

        boolean getSdcardState();

        void getSdCapacity(String uuid);

        void clearSDCard();

    }
}
