package com.cylan.jiafeigou.base.view;

import android.app.Activity;
import android.content.Context;

/**
 * Created by yzd on 16-12-28.
 */

public interface JFGView {

    int VIEW_ACTION_OK = -80000000;
    int VIEW_ACTION_CANCEL = -80000001;
    int VIEW_ACTION_OFFER = -80000002;

    //获取Context对象,该Context一定是可以开启Activity的
    Context getAppContext();

    Activity getActivityContext();

    void showLoadingMsg(String msg);

    void showLoading();

    String showAlert(String title, String msg, String ok, String cancel);

    void showToast(String msg);

    void onScreenRotationChanged(boolean land);

    String onResolveViewLaunchType();

    void onViewAction(int action, String handler, Object extra);

    interface Action {
        void actionDone();
    }

}
