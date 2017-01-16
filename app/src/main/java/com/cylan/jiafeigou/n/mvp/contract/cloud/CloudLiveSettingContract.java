package com.cylan.jiafeigou.n.mvp.contract.cloud;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public interface CloudLiveSettingContract {

    interface View extends BaseView<Presenter> {

        void initSomeViewVisible(boolean isVisible);

        void showClearRecordDialog();

        void showClearRecordProgress();

        void hideClearRecordProgress();

        /**
         * 初始化显示设备名称
         *
         * @param alias
         * @return
         */
        void onCloudInfoRsp(String alias);
    }

    interface Presenter extends BasePresenter {

        boolean isHasBeenShareUser();           //判断是否的被分享的用户

        void clearMesgRecord();

        /**
         * 获取到用户的账号
         */
        Subscription getAccount();

        /**
         * 获取设备名称
         *
         * @return
         */
        String getDeviceName();
    }
}
