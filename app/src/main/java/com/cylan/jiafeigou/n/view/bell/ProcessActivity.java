package com.cylan.jiafeigou.n.view.bell;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.utils.SuperSpUtils;

/**
 * Created by cylan-hunt on 16-8-6.
 */
public class ProcessActivity extends BaseFullScreenFragmentActivity {
    @Override
    protected void onStart() {
        super.onStart();
        SuperSpUtils.getInstance(getApplicationContext())
                .getAppPreferences().put(JConstant.KEY_BELL_CALL_PROCESS_IS_FOREGROUND, 1);
        SuperSpUtils.getInstance(getApplicationContext())
                .getAppPreferences().put(JConstant.KEY_BELL_CALL_PROCESS_ID,
                android.os.Process.myPid());
    }

    @Override
    protected void onStop() {
        super.onStop();
        SuperSpUtils.getInstance(getApplicationContext())
                .getAppPreferences().put(JConstant.KEY_BELL_CALL_PROCESS_IS_FOREGROUND, 0);
    }
}
