package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/**
 * Created by yanzhendong on 2018/1/25.
 */

public interface RegisterFaceContract {

    interface View extends JFGView {
        void onRegisterErrorNoNetwork();

        void onRegisterErrorWrongPictureFormat();

        void onRegisterErrorDetectionFailed();

        void onRegisterSuccessful();

        void onRegisterTimeout();

        void onRegisterErrorPermissionDenied();

        void onRegisterErrorRegisterFailed();

        void onRegisterErrorInvalidParams();

        void onRegisterErrorServerInternalError();

        void onRegisterErrorNoFaceError();

        void onRegisterErrorFaceSmallError();

        void onRegisterErrorMultiFaceError();

        void onRegisterErrorNoFeaturesInFaceError();

        void onRegisterErrorRegUserError();

        void onDeBounceSubmit(boolean enable);
    }

    interface Presenter extends JFGPresenter {

        void performRegisterFaceAction(String nickName, String photoPath);
    }
}
