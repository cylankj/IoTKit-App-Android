package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.PropertyView;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellSettingContract {


    interface View extends PropertyView{
        /**
         * 解绑设备回调
         *
         * @param state
         */
        void unbindDeviceRsp(int state);

        void onClearBellRecordSuccess();

        void onClearBellRecordFailed();
    }

    interface Presenter extends JFGPresenter {
        /**
         * 解绑设备
         */
        void unbindDevice();

        void clearBellRecord(String uuid);
    }
}

