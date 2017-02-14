package com.cylan.jiafeigou.n.mvp.contract.setting;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by cylan-hunt on 17-2-12.
 */

public interface SdcardInfoContract {
    interface View extends BaseView<Presenter> {
    }

    interface Presenter extends BasePresenter {
        void startFormat();
    }
}
