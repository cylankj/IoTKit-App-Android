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

        void unbindDeviceRsp(int state);
    }

    interface Presenter extends BasePresenter {

//        void fetchCamInfo(final String uuid);

        String getDetailsSubTitle(Context context);

        String getAlarmSubTitle(Context context);

        String getAutoRecordTitle(Context context);

        BeanCamInfo getCamInfoBean();

//        @Deprecated//不再使用
//        void saveCamInfoBean(BeanCamInfo camInfoBean, int id);

        /**
         * @param value {@link com.cylan.jiafeigou.dp.BaseValue#setValue(Object)}  }
         * @param id
         */
        void updateInfoReq(Object value, long id);

        void unbindDevice();


    }
}

