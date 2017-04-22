package com.cylan.jiafeigou.push;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.cylan.jiafeigou.push.google.RegistrationIntentService;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PushPickerIntentService extends IntentService {
    private static final String TAG = "PushPickerIntentService";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public PushPickerIntentService() {
        super("PushPickerIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            //首选谷歌服务
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
                AppLogger.d("yes pick gcm");
                return;
            }
            //接入华为推送

        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
//                apiAvailability.getErrorDialog(this,
//                        resultCode,
//                        PLAY_SERVICES_RESOLUTION_REQUEST)
//                        .show();
                Log.i(TAG, "This device is not .");
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }
}
