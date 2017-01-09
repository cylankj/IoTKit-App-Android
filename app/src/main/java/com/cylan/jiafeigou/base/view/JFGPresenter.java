package com.cylan.jiafeigou.base.view;

/**
 * Created by yzd on 16-12-28.
 */

public interface JFGPresenter {

    void onViewAttached(JFGView view);

    void onStart();

    void onSetContentView();

    void onStop();

    void onViewDetached();

    void onViewAction(int action, String handle, Object extra);

    void onScreenRotationChanged(boolean land);

    void onSetViewUUID(String uuid);
}
