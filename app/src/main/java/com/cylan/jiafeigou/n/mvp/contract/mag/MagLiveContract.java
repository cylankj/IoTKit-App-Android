package com.cylan.jiafeigou.n.mvp.contract.mag;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/10/20
 * 描述：
 */
public interface MagLiveContract {

    interface View extends BaseView<Presenter>{

    }

    interface Presenter extends BasePresenter{
        boolean getDoorCurrentState();
    }

}
