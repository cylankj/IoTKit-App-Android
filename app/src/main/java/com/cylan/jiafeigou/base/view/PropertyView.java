package com.cylan.jiafeigou.base.view;

/**
 * Created by yzd on 17-1-9.
 */

public interface PropertyView<T extends JFGDevice> extends JFGView {
    void onShowProperty(T device);
}
