package com.cylan.jiafeigou.misc;

import android.content.Intent;

import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.n.engine.DataSourceService;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

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
        final long time = System.currentTimeMillis();
        JfgAppCmd jfgAppCmd = JfgAppCmd.getInstance();
        if (jfgAppCmd == null) {
            AppLogger.e("jfgAppCmd is null");
            ContextUtils.getContext().startService(new Intent(ContextUtils.getContext(), DataSourceService.class));
        } else return jfgAppCmd;
        while ((jfgAppCmd = JfgAppCmd.getInstance()) == null) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
        AppLogger.i("getCmd: " + (System.currentTimeMillis() - time));
        return jfgAppCmd;
    }
}
