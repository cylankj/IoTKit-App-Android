package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.base.module.JFGDoorBellDevice;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.PropertyView;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface DoorBellHomeContract {


    interface View extends PropertyView<JFGDoorBellDevice> {

        /**
         * 电量提醒
         */
        void onBellBatteryDrainOut();

        void onRecordsListRsp(ArrayList<BellCallRecordBean> beanArrayList);

    }

    interface Presenter extends JFGPresenter {

        void fetchBellRecordsList(boolean asc, long time);

        void deleteBellCallRecord(List<BellCallRecordBean> list);
    }
}

