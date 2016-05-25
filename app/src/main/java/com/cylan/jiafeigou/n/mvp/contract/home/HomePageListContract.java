package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.util.List;

/**
 * Created by hunt on 16-5-23.
 */
public interface HomePageListContract {

    interface View extends BaseView<Presenter> {


        void onDeviceListRsp(List<BaseBean> resultList);

    }

    interface Presenter extends BasePresenter {
        void startRefresh();
    }
}
