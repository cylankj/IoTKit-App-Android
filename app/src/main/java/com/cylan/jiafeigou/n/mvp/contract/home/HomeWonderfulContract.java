package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.jiafeigou.n.mvp.model.impl.HomeWonderfulModelImpl;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.util.List;

/**
 * Created by hunt on 16-5-23.
 */
public interface HomeWonderfulContract {

    interface View extends BaseView<Presenter> {
        void onDeviceListRsp(List<MediaBean> list);

        void onHeadBackgroundChang(int daytime);

        void onGetBroadcastReceiver(HomeWonderfulModelImpl homeWonderfulModelImpl);
    }

    interface Presenter extends BasePresenter {
        void startRefresh();
    }

    interface PresenterRequiredOps {

        void changeHeadBackground(int daytime);
    }
}
