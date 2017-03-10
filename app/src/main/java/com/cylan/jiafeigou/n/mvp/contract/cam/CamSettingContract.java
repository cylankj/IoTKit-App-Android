package com.cylan.jiafeigou.n.mvp.contract.cam;

import android.content.Context;

import com.cylan.jiafeigou.base.module.JFGCameraDevice;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface CamSettingContract {


    interface View extends BaseView<Presenter> {

        void deviceUpdate(JFGCameraDevice jfgCameraDevice);

        void unbindDeviceRsp(int state);

        void onNetworkChanged(boolean connected);
    }

    interface Presenter extends BasePresenter {

//        void fetchCamInfo(final String uuid);

        String getDetailsSubTitle(Context context);

        String getAlarmSubTitle(Context context);

        String getAutoRecordTitle(Context context);

//        BeanCamInfo getCamInfoBean();

//        @Deprecated//不再使用
//        void saveCamInfoBean(BeanCamInfo camInfoBean, int dpMsgId);

        /**
         * @param value {@link com.cylan.jiafeigou.dp.BaseValue#setValue(Object)}  }
         * @param id
         */
//        void updateInfoReq(Object value, long dpMsgId);
        public <T extends DataPoint> void updateInfoReq(T value, long id);

        void unbindDevice();


    }
}

