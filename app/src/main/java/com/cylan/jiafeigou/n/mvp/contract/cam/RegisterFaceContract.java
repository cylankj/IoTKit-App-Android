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
    }

    interface Presenter extends JFGPresenter {

        void performRegisterFaceAction(String nickName, String photoPath);
    }
}
