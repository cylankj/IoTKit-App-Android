package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

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

        boolean switchVoice();

        boolean switchShake();

        void initSwitchState(RxEvent.GetUserInfo userInfo);
    }

    interface Presenter extends BasePresenter {

        void clearCache();

        void calculateCacheSize();

        boolean getNegation();

        void savaSwitchState(boolean isChick, String key);

        /**
         * 获取到用户的信息
         * @return
         */
        Subscription getAccountInfo();
    }

}
