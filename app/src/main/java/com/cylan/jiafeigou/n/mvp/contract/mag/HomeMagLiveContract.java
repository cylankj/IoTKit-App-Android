package com.cylan.jiafeigou.n.mvp.contract.mag;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/19
 * 描述：
 */
public interface HomeMagLiveContract {

    interface View extends BaseView<Presenter> {
        boolean openDoorNotify();           //打开开和关通知

        void initMagDoorStateNotify();

        /**
         * 消息记录为空
         */
        void showNoMesg();

        /**
         * 显示清除进度
         */
        void showClearProgress();

        /**
         * 隐藏清除进度
         */
        void hideClearProgress();
    }

    interface Presenter extends BasePresenter {

        void clearOpenAndCloseRecord();             //清空开和关的记录

        boolean getNegation();

        void saveSwitchState(boolean isChick, String key);

        boolean getSwitchState(String key);

        /**
         * 获取到用户的信息拿到数据库对象
         */
        Subscription getAccount();
    }

}
