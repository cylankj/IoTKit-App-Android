package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.base.module.JFGDoorBellDevice;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.PropertyView;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellDetailContract {


    interface View extends PropertyView<JFGDoorBellDevice> {
    }

    interface Presenter extends JFGPresenter {
        BeanBellInfo getBellInfo();

        void saveBellInfo(BeanBellInfo info, int id);
    }
}

