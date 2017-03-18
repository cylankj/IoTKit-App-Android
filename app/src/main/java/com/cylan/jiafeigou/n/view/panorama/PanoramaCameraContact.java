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

        void onShortVideoStarted();

        void onShortVideoCompleted();

        void onShortVideoCanceled(int reason);

        void onLongVideoStarted();

        void onLongVideoCompleted();

        void onMakePhotoGraphFailed();

        void onStartShortVideoFailed();

        void onStartMakeVideoFailed();

        void onSetShortVideoRecordLayout();

        void onSetLongVideoRecordLayout();

        void onStopMakeVideoFailed();


        enum SPEED_MODE {AUTO, FLUENCY, NORMAL, HD}

        enum CONNECTION_MODE {FINE, DEVICE_OFFLINE, BAD_NETWORK}

        enum PANORAMA_VIEW_MODE {MODE_PICTURE, MODE_VIDEO}

        enum PANORAMA_RECORD_MODE {MODE_NONE, MODE_SHORT, MODE_LONG}

        void onSwitchSpeedMode(SPEED_MODE mode);

        void onSDCardUnMounted();

        void onSDCardMemoryFull();

        void onDeviceBatteryLow();

        void onUpdateRecordTime(int second, int type);

        void onMakePhotoGraphPreview();

        void onMakePhotographSuccess(Bitmap picture);


    }

    interface Presenter extends ViewablePresenter {

        class RecordProgress {
            long second;

            public RecordProgress(long second) {
                this.second = second;
            }
        }

        class RecordFinished {
        }

        void makePhotograph();//拍照

        void startMakeLongVideo();//开始录制长视频

        void stopMakeLongVideo();

        void startMakeShortVideo();//开始录制短视频

        void stopMakeShortVideo();

        void checkAndInitRecord();

    }
}
