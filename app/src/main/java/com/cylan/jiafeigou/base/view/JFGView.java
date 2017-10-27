package com.cylan.jiafeigou.base.view;

import android.app.Activity;

/**
 * Created by yzd on 16-12-28.
 */

public interface JFGView {

    int VIEW_ACTION_OK = -80000000;
    int VIEW_ACTION_CANCEL = -80000001;

    Activity activity();
    void onLoginStateChanged(boolean online);

    interface Action {
        void actionDone();
    }

}
