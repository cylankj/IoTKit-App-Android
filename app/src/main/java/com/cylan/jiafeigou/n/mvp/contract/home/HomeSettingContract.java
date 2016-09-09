package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.io.File;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public interface HomeSettingContract {

    interface View extends BaseView<Presenter>{
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
        void initSwitchState();
    }

    interface Presenter extends BasePresenter{
        void clearCache();
        void calculateCacheSize();
        boolean getNegation();
        void savaSwitchState(boolean isChick,String key);
        boolean getSwitchState(String key);
    }

}
