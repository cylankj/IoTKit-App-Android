package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface DoorBellHomeContract {


    interface View extends BaseView<Presenter> {

        void onLoginState(int state);

        void onRecordsListRsp(ArrayList<BellCallRecordBean> beanArrayList);

    }

    interface Presenter extends BasePresenter {

        void fetchBellRecordsList();

    }
}

