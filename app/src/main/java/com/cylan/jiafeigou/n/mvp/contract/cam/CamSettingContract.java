package com.cylan.jiafeigou.n.mvp.contract.cam;

import android.content.Context;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface CamSettingContract {


    interface View extends BaseView<Presenter> {

        void onCamInfoRsp(BeanCamInfo timeSet);

        void isSharedDevice();
    }

    interface Presenter extends BasePresenter {

//        void fetchCamInfo(final String uuid);

        String getDetailsSubTitle(Context context);

        String getAlarmSubTitle(Context context);

        String getAutoRecordTitle(Context context);

        BeanCamInfo getCamInfoBean();

        void saveCamInfoBean(BeanCamInfo camInfoBean, int id);
    }
}

