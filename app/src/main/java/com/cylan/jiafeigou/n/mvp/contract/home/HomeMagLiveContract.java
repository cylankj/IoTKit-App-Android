package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/19
 * 描述：
 */
public interface HomeMagLiveContract {

    interface View extends BaseView<Presenter>{

    }

    interface Presenter extends BasePresenter{
        void clearOpenAndCloseRecord();             //清空开和关的记录
    }

}
