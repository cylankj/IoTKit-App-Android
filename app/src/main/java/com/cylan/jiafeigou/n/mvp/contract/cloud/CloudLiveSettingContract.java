package com.cylan.jiafeigou.n.mvp.contract.cloud;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

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
    }

    interface Presenter extends BasePresenter {
        boolean isHasBeenShareUser();           //判断是否的被分享的用户

        void clearMesgRecord();
    }
}
