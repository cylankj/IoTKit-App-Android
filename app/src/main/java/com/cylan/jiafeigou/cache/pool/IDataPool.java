package com.cylan.jiafeigou.cache.pool;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.dp.IDataPoint;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public interface IDataPool extends IDataPoint {
    /**
     * Map<account+uuid,JFGDevice>
     *
     * @param jfgDevice
     */
    void cacheDevice(JFGDevice jfgDevice);

    /**
     * 内部转换 Map<account+uuid,JFGDevice>
     * @param uuid
     * @return
     */
    JFGDevice fetch(String uuid);
}
