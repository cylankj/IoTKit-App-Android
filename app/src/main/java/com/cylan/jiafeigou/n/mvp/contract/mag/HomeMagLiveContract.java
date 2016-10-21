package com.cylan.jiafeigou.n.mvp.contract.mag;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/19
 * 描述：
 */
public interface HomeMagLiveContract {

    interface View extends BaseView<Presenter> {
        boolean openDoorNotify();           //打开开和关通知

        void initMagDoorStateNotify();
    }

    interface Presenter extends BasePresenter {
        void clearOpenAndCloseRecord();             //清空开和关的记录

        boolean getNegation();

        void saveSwitchState(boolean isChick, String key);

        boolean getSwitchState(String key);
    }

}
