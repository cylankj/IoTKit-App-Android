package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.dp.DpMsgDefine;

/**
 * Created by yanzhendong on 2018/1/26.
 */

public interface FaceSettingContract {

    interface View extends JFGView {

        void onQueryFaceDetectionFinished(Boolean isFaceDetectionOpened);

        void onChangeFaceDetectionFinished(Boolean isSuccessful);

        void onQueryFaceDetectionSizeFinished(DpMsgDefine.DPDetectionSize size);

        void onChangeFaceDetectionSizeFinished(Boolean success, DpMsgDefine.DPDetectionSize detectionSize);
    }

    interface Presenter extends JFGPresenter {

        void performCheckAndInitFaceSetting();

        void performChangeFaceDetectionAction(boolean isChecked);

        void performChangeFaceDetectionSizeAction(DpMsgDefine.DPDetectionSize detectionSize);
    }
}
