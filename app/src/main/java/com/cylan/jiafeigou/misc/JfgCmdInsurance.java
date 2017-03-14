package com.cylan.jiafeigou.misc;

import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.n.engine.DataSourceService;
import com.cylan.jiafeigou.support.log.AppLogger;

/**
 * Created by cylan-hunt on 16-10-13.
 */

public class JfgCmdInsurance {

    /**
     * 必须在非ui线程中调用
     *
     * @return
     */
    public static JfgAppCmd getCmd() {
        JfgAppCmd jfgAppCmd = JfgAppCmd.getInstance();
        if (jfgAppCmd == null) {
            DataSourceService.getInstance().initNative();
            jfgAppCmd = JfgAppCmd.getInstance();
            AppLogger.e("jfgAppCmd is null: " + jfgAppCmd);
        } else return jfgAppCmd;
        return jfgAppCmd;
    }
}
