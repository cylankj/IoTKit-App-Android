package com.cylan.jiafeigou.n.mvp.contract.cam;

import android.content.Context;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.misc.SettingTip;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.io.IOException;
import java.util.List;

import rx.Observable;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface CamSettingContract {


    interface View extends BaseView<Presenter> {

        void deviceUpdate(Device device);

        void deviceUpdate(JFGDPMsg msg) throws IOException;

        void unbindDeviceRsp(int state);

        void onNetworkChanged(boolean connected);
    }

    interface Presenter extends BasePresenter {

        void updateSettingTips(SettingTip settingTip);

        SettingTip getSettingTips();
//        void fetchCamInfo(final String uuid);

        String getDetailsSubTitle(Context context, boolean hasSdcard, int err);

        String getAlarmSubTitle(Context context);

        String getAutoRecordTitle(Context context);

//        BeanCamInfo getCamInfoBean();

//        @Deprecated//不再使用
//        void saveCamInfoBean(BeanCamInfo camInfoBean, int msgId);

        /**
         * @param value {@link com.cylan.jiafeigou.dp.BaseValue#setValue(Object)}  }
         * @param id
         */
//        void updateInfoReq(Object value, long msgId);
        <T extends DataPoint> void updateInfoReq(T value, long id);

        <T extends DataPoint> void updateInfoReq(List<T> value);

        void unbindDevice();

        Observable<Boolean> enableAp();
    }
}

