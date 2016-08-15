package com.cylan.jiafeigou.n.engine;

import android.app.IntentService;
import android.content.Intent;

import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.utils.AppLogger;

/**
 * 一个空的service,不添加任何逻辑。它和{@link BellLiveActivity}属于
 * 同一个进程，在进入门铃主页列表前，需要创建一个这样的新进程，BellCallActivity过程动画才有效。
 */
public class SimpleHelperIntentService extends IntentService {


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SimpleHelperIntentService() {
        super("SimpleHelperIntentService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppLogger.d("i am a road man");
    }


}
