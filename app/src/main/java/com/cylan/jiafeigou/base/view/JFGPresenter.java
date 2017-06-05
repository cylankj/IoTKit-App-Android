package com.cylan.jiafeigou.base.view;

/**
 * Created by yzd on 16-12-28.
 */

public interface JFGPresenter<V extends JFGView> {

    void onViewAttached(V view);

    void onStart();

    void onResume();

    void onSetContentView();

    void onPause();

    void onStop();

    void onViewDetached();

    void onViewAction(int action, String handle, Object extra);

    void onScreenRotationChanged(boolean land);

    void onSetViewUUID(String uuid);
}
