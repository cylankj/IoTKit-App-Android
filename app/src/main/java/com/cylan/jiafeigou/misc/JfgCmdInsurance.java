package com.cylan.jiafeigou.misc;

import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.n.engine.DataSource;
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
            AppLogger.e("jfgAppCmd is null");
            DataSource.getInstance().initNative();
            jfgAppCmd = JfgAppCmd.getInstance();
        } else return jfgAppCmd;
        return jfgAppCmd;
    }
}
