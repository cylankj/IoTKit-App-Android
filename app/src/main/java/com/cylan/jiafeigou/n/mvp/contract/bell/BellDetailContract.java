package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellDetailContract {


    interface View extends BaseView<Presenter> {

        void onSettingInfoRsp(BeanBellInfo bellInfoBean);


    }

    interface Presenter extends BasePresenter {
        BeanBellInfo getBellInfo();

        void saveBellInfo(BeanBellInfo info, int id);
    }
}

