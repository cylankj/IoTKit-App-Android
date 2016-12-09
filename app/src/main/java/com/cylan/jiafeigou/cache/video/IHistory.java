package com.cylan.jiafeigou.cache.video;

import com.cylan.jiafeigou.dp.IParser;

/**
 * Created by cylan-hunt on 16-12-6.
 */

public interface IHistory extends IParser {

    String IHistory = "IHistory:";

    /**
     * 清空历史数据
     */
    void clear();

}
