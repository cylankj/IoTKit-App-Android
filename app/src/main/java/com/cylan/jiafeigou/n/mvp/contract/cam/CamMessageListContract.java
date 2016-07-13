package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface CamMessageListContract {


    interface View extends BaseView<Presenter> {

        void onMessageListRsp(ArrayList<CamMessageBean> beanArrayList);

    }

    interface Presenter extends BasePresenter {
        void fetchMessageList();
    }
}

