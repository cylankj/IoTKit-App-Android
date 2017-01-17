package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.base.module.JFGDoorBellDevice;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.PropertyView;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellDetailContract {


    interface View extends PropertyView<JFGDoorBellDevice> {
    }

    interface Presenter extends JFGPresenter {
        //        void saveBellInfo(BeanBellInfo info, int id);
        void updateInfoReq(String uuid, Object value, long id);
    }
}

