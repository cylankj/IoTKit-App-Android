package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public interface SdCardInfoContract {

    interface View extends BaseView<Presenter>{

        void sdUseDetail(String volume,float data);

        void showLoading();

        void hideLoading();

        void clearSdResult(int code);
    }


    interface Presenter extends BasePresenter{

        boolean getSdcardState();

        void clearSDcard(int id);

        void clearCountTime();

        Subscription onClearSdBack();
    }
}
