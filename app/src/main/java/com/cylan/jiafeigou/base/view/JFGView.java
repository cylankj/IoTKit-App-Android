package com.cylan.jiafeigou.base.view;

import android.app.Activity;
import android.content.Context;

/**
 * Created by yzd on 16-12-28.
 */

public interface JFGView {

    int VIEW_ACTION_OK = 0;
    int VIEW_ACTION_CANCEL = 1;


    //获取Context对象,该Context一定是可以开启Activity的
    Context getAppContext();

    Activity getActivityContext();

    void showLoadingMsg(String msg);

    void showLoading();

    String showAlert(String title, String msg, String ok, String cancel);

    void showToast(String msg);

    void onScreenRotationChanged(boolean land);

    String onResolveViewLaunchType();

    interface Action {
        void actionDone();
    }

}
