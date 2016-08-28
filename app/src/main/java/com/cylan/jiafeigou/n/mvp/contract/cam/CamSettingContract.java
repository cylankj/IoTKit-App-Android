package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.CamInfoBean;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface CamSettingContract {


    interface View extends BaseView<Presenter> {

        void onCamInfoRsp(CamInfoBean timeSet);

    }

    interface Presenter extends BasePresenter {

        void fetchCamInfo(final String cid);

        CamInfoBean getCamInfoBean();

        void saveCamInfoBean(CamInfoBean camInfoBean);
    }
}

