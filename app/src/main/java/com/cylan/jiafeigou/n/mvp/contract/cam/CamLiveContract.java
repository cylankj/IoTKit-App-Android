package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.widget.wheel.SDataStack;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface CamLiveContract {


    interface View extends BaseView<Presenter> {

        void onHistoryDataRsp(SDataStack timeSet);

    }

    interface Presenter extends BasePresenter {
        void fetchHistoryData();
    }
}

