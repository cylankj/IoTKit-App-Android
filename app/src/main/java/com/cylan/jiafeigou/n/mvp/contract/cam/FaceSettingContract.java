package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/**
 * Created by yanzhendong on 2018/1/26.
 */

public interface FaceSettingContract {

    interface View extends JFGView {

        void onQueryFaceDetectionFinished(Boolean isFaceDetectionOpened);

        void onChangeFaceDetectionFinished(Boolean isSuccessful);
    }

    interface Presenter extends JFGPresenter {

        void performCheckFaceDetectionSetting();

        void performChangeFaceDetectionAction(boolean isChecked);
    }
}
