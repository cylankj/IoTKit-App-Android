package com.cylan.jiafeigou.n.mvp.contract.setting;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;

/**
 * Created by hunt on 16-5-26.
 */

public interface SafeInfoContract {

    interface View extends BaseView<Presenter> {
        void beanUpdate(BeanCamInfo info);
    }

    interface Presenter extends BasePresenter {
        void save(BeanCamInfo beanCamInfo);

        BeanCamInfo getBean();
    }
}
