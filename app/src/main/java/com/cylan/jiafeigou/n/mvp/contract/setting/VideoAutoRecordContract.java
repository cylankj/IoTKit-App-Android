package com.cylan.jiafeigou.n.mvp.contract.setting;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by hunt on 16-5-26.
 */

public interface VideoAutoRecordContract {

    interface View extends BaseView<Presenter> {
    }

    interface Presenter extends BasePresenter {

        /**
         * @param value {@link com.cylan.jiafeigou.dp.BaseValue#setValue(Object)}  }
         * @param id
         */
        void updateInfoReq(Object value, long id);

    }
}
