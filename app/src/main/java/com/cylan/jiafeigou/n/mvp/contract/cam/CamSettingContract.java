package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface CamSettingContract {


    interface View extends BaseView<Presenter> {

        void onCamInfoRsp(BeanCamInfo timeSet);

        void isSharedDevice();
    }

    interface Presenter extends BasePresenter {

        void fetchCamInfo(final DeviceBean cid);

        BeanCamInfo getCamInfoBean();

        void saveCamInfoBean(BeanCamInfo camInfoBean);
    }
}

