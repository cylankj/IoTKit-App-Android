package com.cylan.jiafeigou.n.view.panorama;

import android.graphics.Bitmap;

import com.cylan.jiafeigou.base.module.JFGCameraDevice;
import com.cylan.jiafeigou.base.view.PropertyView;
import com.cylan.jiafeigou.base.view.ViewablePresenter;
import com.cylan.jiafeigou.base.view.ViewableView;

/**
 * Created by yanzhendong on 2017/3/7.
 */

public interface PanoramaCameraContact {

    interface View extends PropertyView<JFGCameraDevice>, ViewableView {

        void onNetWorkChangedToMobile();

        void onNetWorkChangedToWiFi();

        enum SPEED_MODE {AUTO, FLUENCY, NORMAL, HD}

        enum CONNECTION_MODE {FINE, DEVICE_OFFLINE, BAD_NETWORK}

        enum PANORAMA_VIEW_MODE {MODE_PICTURE, MODE_VIDEO}

        void onSwitchSpeedMode(SPEED_MODE mode);

        void onSDCardUnMounted();

        void onSDCardMemoryFull();

        void onDeviceBatteryLow();

        void onUpdateRecordTime(int second);

        void onMakePhotoGraphPreview();

        void onMakePhotographSuccess(Bitmap picture);


    }

    interface Presenter extends ViewablePresenter {
        void makePhotograph();//拍照

        void startMakeLongVideo();//开始录制长视频

        void stopMakeMakeLongVideo();

        void startMakeShortVideo();//开始录制短视频

        void stopMakeShortVideo();
    }
}
