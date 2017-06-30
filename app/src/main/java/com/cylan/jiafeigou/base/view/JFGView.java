package com.cylan.jiafeigou.base.view;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;

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

    void showLoading(int resId, Object... args);

    void hideLoading();

    AlertDialog getAlert();

    void showToast(String msg);

    void onScreenRotationChanged(boolean land);

    String onResolveViewLaunchType();

    void onLoginStateChanged(boolean online);


    void onViewAction(int action, String handler, Object extra);

    interface Action {
        void actionDone();
    }

}
