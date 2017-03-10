package com.cylan.jiafeigou.base.view;

import com.cylan.jiafeigou.cache.db.module.Device;

/**
 * Created by yzd on 17-1-9.
 */

public interface PropertyView<T extends Device> extends JFGView {
    void onShowProperty(T device);
}
