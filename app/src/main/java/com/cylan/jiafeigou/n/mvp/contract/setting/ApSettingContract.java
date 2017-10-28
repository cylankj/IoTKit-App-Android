package com.cylan.jiafeigou.n.mvp.contract.setting;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by hds on 17-9-7.
 */

public class ApSettingContract {

    public interface View extends BaseView {
        void timeout();

        void success();

        String getHotSpotName();
    }

    public interface Presenter extends BasePresenter {

        void monitorHotSpot();
    }
}
