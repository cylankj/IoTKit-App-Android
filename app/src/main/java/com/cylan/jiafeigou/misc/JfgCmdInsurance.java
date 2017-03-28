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
        JfgAppCmd jfgAppCmd = JfgAppCmd.getInstance();
        if (jfgAppCmd == null) {
            ContextUtils.getContext().startService(new Intent(ContextUtils.getContext(), DataSourceService.class));
            while (JfgAppCmd.getInstance() == null) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            jfgAppCmd = JfgAppCmd.getInstance();
            AppLogger.e("jfgAppCmd is null: " + jfgAppCmd);
        } else return jfgAppCmd;
        return jfgAppCmd;
    }
}
