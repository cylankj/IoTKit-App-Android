package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface DoorBellHomeContract {


    interface View extends BaseView<Presenter> {

        void onLoginState(boolean state);

        /**
         * 电量提醒
         */
        void onBellBatteryDrainOut();

        void onRecordsListRsp(ArrayList<BellCallRecordBean> beanArrayList);

    }

    interface Presenter extends BasePresenter {

        void fetchBellRecordsList();

        int getDeviceNetState();

        void setBellInfo(DeviceBean bean);

        BeanBellInfo getBellInfo();
    }
}

