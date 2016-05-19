package com.cylan.jiafeigou.n.base;

import android.app.IntentService;
import android.content.Intent;

/**
 * 这个Service只在app启动时启动,作为初始化工程中使用的各大模块,组件,任务.
 * 1.日志模块
 * 2.Bugly模块
 * 3.BlockCanary
 * 4.Mta
 * 5.中性包配置文件.
 * 6.图片管理加载库
 * 7.StrictMode
 */
public class FirstTaskInitService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p>
     * Used to name the worker thread, important only for debugging.
     * <p>
     */
    public FirstTaskInitService() {
        super("TaskInitService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

}
