package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.PropertyView;
import com.cylan.jiafeigou.base.view.ViewablePresenter;
import com.cylan.jiafeigou.base.view.ViewableView;

/**
 * Created by yanzhendong on 2017/3/7.
 */

public interface PanoramaCameraContact {

    interface View extends PropertyView, ViewableView {
        enum SPEED_MODE {AUTO, FLUENCY, NORMAL, HD}

        enum CONNECTION_MODE {FINE, DEVICE_OFFLINE, BAD_NETWORK}

        void onSwitchSpeedMode(SPEED_MODE mode);
    }

    interface Presenter extends ViewablePresenter {

    }
}
