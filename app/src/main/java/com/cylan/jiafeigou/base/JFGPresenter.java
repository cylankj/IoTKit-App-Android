package com.cylan.jiafeigou.base;

/**
 * Created by yzd on 16-12-28.
 */

public interface JFGPresenter<V extends JFGView> {

    void onViewAttached(V view);

    void onStart();

    void onStop();

    void onViewDetached();

    void onViewAction(int action, Object extra);

    void onScreenRotationChanged(boolean land);
}
