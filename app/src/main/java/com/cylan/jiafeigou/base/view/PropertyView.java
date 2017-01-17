package com.cylan.jiafeigou.base.view;

import com.cylan.jiafeigou.base.module.JFGDPDevice;

/**
 * Created by yzd on 17-1-9.
 */

public interface PropertyView<T extends JFGDPDevice> extends JFGView {
    void onShowProperty(T device);
}
