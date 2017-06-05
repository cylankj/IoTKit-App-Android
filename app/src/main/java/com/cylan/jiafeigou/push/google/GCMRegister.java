package com.cylan.jiafeigou.push.google;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.cylan.jiafeigou.push.IPushRegister;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import static com.cylan.jiafeigou.push.PushConstant.PUSH_TAG;
import static com.cylan.jiafeigou.push.google.QuickstartPreferences.PUSH_MS_NAME;
import static com.cylan.jiafeigou.push.google.QuickstartPreferences.PUSH_TOKEN;

/**
 * Created by hds on 17-4-21.
 */

public class GCMRegister extends IntentService implements IPushRegister {
    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public GCMRegister() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String id = PackageUtils.getMetaString(getApplicationContext(), "GCM_APP_ID");
            AppLogger.d("gcm appId: " + id);
            if (TextUtils.isEmpty(id)) throw new IllegalArgumentException("gcm appId is null");
            String token = instanceID.getToken(id, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            AppLogger.d(PUSH_TAG + "GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            AppLogger.d(PUSH_TAG + "Failed to complete token fetch: " + MiscUtils.getErr(e));
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     * <p>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        Intent intent = new Intent();
        intent.setAction(PUSH_TOKEN);
        intent.putExtra(PUSH_TOKEN, token);
        intent.putExtra(PUSH_MS_NAME, "GCM");
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

    @Override
    public void registerPMS(Context context) {

    }

    @Override
    public void releasePMS() {

    }
    // [END subscribe_topics]
}
