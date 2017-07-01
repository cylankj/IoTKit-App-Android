package com.cylan.jiafeigou.push;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.push.google.GCMRegister;
import com.cylan.jiafeigou.push.mi.XiaoMiHMSRegister;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static com.cylan.jiafeigou.push.PushConstant.PUSH_TAG;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PushPickerIntentService extends IntentService {
    private static final String TAG = "PushPickerIntentService";

    public PushPickerIntentService() {
        super("PushPickerIntentService");
    }

    public static void start() {
        Context ctx = ContextUtils.getContext();
        Intent intent = new Intent(ctx, PushPickerIntentService.class);
        ctx.startService(intent);
    }

    private boolean shouldInit() {
        Log.d("shouldInit", "shouldInit:" + Build.MANUFACTURER);
        return false;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            //首选谷歌服务
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                intent = new Intent(this, GCMRegister.class);
                startService(intent);
                AppLogger.d(PUSH_TAG + "yes pick gcm");
                return;
            }
            boolean isEMUI_HW_MODEL = BuildProperties.EMUIUtils.isEMUI();
            if (isEMUI_HW_MODEL) {
                //接入华为推送
                intent = new Intent(this, HMSRegister.class);
                startService(intent);
                AppLogger.d(PUSH_TAG + "yes pick hms");
                return;
            }
            boolean isMIUI_XIAOMI_MODEL = BuildProperties.MIUIUtils.isMIUI();
            if (isMIUI_XIAOMI_MODEL) {
                //接入小米推送
                intent = new Intent(this, XiaoMiHMSRegister.class);
                startService(intent);
                AppLogger.d(PUSH_TAG + "yes pick xiaomi");
                return;
            }
            boolean is_Flyme = BuildProperties.FlymeUtils.isFlyme();
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device'account system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        AppLogger.d(PUSH_TAG + "resultCode:" + resultCode);
        switch (resultCode) {
            case ConnectionResult.SUCCESS:
                return true;
            case ConnectionResult.SERVICE_DISABLED:
            case ConnectionResult.SERVICE_INVALID:
            case ConnectionResult.SERVICE_MISSING:
                AppLogger.d(PUSH_TAG + " 谷歌服务异常:" + resultCode);
                return false;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                AppLogger.d(PUSH_TAG + " 谷歌服务需要升级");
                if (apiAvailability.isUserResolvableError(resultCode)) {
                    long lastTime = PreferencesUtils.getLong(JConstant.SHOW_GCM_DIALOG);
                    if (System.currentTimeMillis() - lastTime >= 24 * 3600 * 1000) {
                        PreferencesUtils.putLong(JConstant.SHOW_GCM_DIALOG, System.currentTimeMillis());
                        RxBus.getCacheInstance().postSticky(new RxEvent.NeedUpdateGooglePlayService());
                    }
                    AppLogger.d(PUSH_TAG + " This device support gcm but get err");
                } else {
                    AppLogger.d(PUSH_TAG + " This device is not supported");
                }
                return false;
        }
        return true;
    }


}
