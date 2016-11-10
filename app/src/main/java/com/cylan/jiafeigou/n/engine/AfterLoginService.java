package com.cylan.jiafeigou.n.engine;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 * 登陆成功后，需要刷新一些缓存，数据，都在这里做。
 */
public class AfterLoginService extends IntentService {

    private static final String TAG = "KEY";
//    private static final String KEY_ACCOUNT = "key_account";
//    private static final String KEY_PWD = "key_account";
    /**
     * 保存账号密码，登陆成功后保存。
     */
    public static final String ACTION_SAVE_ACCOUNT = "action_save_account";
    public static final String ACTION_GET_ACCOUNT = "action_get_account";

    public AfterLoginService() {
        super("AfterLoginService");
    }

    public static void startSaveAccountAction(Context context) {
        Intent intent = new Intent(context, AfterLoginService.class);
        intent.putExtra(TAG, ACTION_SAVE_ACCOUNT);
        context.startService(intent);
    }

    public static void startGetAccountAction(Context context) {
        Intent intent = new Intent(context, AfterLoginService.class);
        intent.putExtra(TAG, ACTION_GET_ACCOUNT);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getStringExtra(TAG);
            if (TextUtils.equals(action, ACTION_SAVE_ACCOUNT)) {
                LoginAccountBean l = JCache.tmpAccount;
                if (l == null || TextUtils.isEmpty(l.userName) || TextUtils.isEmpty(l.pwd)) {
                    AppLogger.i("do nothing");
                    return;
                }
                PreferencesUtils.putString("wth_a", l.userName);
                PreferencesUtils.putString("wth_p", l.pwd);
                //
            } else if (TextUtils.equals(action, ACTION_GET_ACCOUNT)) {
                JfgCmdInsurance.getCmd().getAccount();
            }
        }
    }


}
