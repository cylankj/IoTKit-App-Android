package com.cylan.jiafeigou.push.google;

import android.os.Bundle;
import android.util.Log;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.push.BellPuller;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by hds on 17-4-21.
 */

public class GcmService extends GcmListenerService {
    private static final String TAG = "GcmService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        JFGSourceManager sourceManager = DataSourceManager.getInstance();
        Log.d(TAG, "From: " + from + ",login?" + (sourceManager == null));
        Log.d(TAG, "DpMessage: " + message);
        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }
        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message);
        BellPuller.getInstance().fireBellCalling(getApplicationContext(), message, data);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Log.d(TAG, TAG + ",gcm is coming: " + message);
    }

}
