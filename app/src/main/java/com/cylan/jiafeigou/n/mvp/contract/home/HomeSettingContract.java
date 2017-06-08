package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.rx.RxEvent;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public interface HomeSettingContract {

    interface View extends BaseView<Presenter> {

        void showLoadCacheSizeProgress();

        void hideLoadCacheSizeProgress();

        void setCacheSize(String size);

        void showClearingCacheProgress();

        void hideClearingCacheProgress();

        void clearFinish();

        void clearNoCache();

        boolean switchAcceptMesg();

        void initSwitchState(RxEvent.AccountArrived accountArrived);
    }

    interface Presenter extends BasePresenter {

        /**
         * 清理缓存
         */
        void clearCache();

        /**
         * 计算缓存的大小
         */
        void calculateCacheSize();

        /**
         * 取反
         *
         * @return
         */
        boolean getNegation();

        /**
         * 保存开关状态
         *
         * @param isChick
         * @param key
         */
        void savaSwitchState(boolean isChick, String key);

        /**
         * 获取到用户的信息
         *
         * @return
         */
        Subscription getAccountInfo();

        void refreshWechat();

    }

}
