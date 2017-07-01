package com.cylan.jiafeigou.push.google;

import android.content.Context;
import android.util.Log;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static com.cylan.jiafeigou.push.PushConstant.PUSH_TAG;

/**
 * Created by hds on 17-4-21.
 */

public class GPlayServiceChecker {

    private static final String TAG = "GPlayServiceChecker";

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device'account system settings.
     */
    public static boolean checkPlayServices(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        AppLogger.d(PUSH_TAG + "resultCode:" + resultCode);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
//                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
//                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                AppLogger.d("This device is not supported.");
            }
            return false;
        }
        return true;
    }
}
